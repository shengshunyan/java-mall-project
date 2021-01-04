package com.guoshi.mall.service.impl;

import com.google.gson.Gson;
import com.guoshi.mall.dao.ProductMapper;
import com.guoshi.mall.enums.ProductStatusEnum;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.form.CartAddForm;
import com.guoshi.mall.form.CartUpdateForm;
import com.guoshi.mall.pojo.Cart;
import com.guoshi.mall.pojo.Product;
import com.guoshi.mall.service.ICartService;
import com.guoshi.mall.vo.CartProductVo;
import com.guoshi.mall.vo.CartVo;
import com.guoshi.mall.vo.ResponseVo;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements ICartService {

    // redis key 常量
    private final static String CART_REDIS_KEY_TEMPLATE = "cart_%d";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Gson gson = new Gson();

    @Override
    public ResponseVo<CartVo> add(Integer uid, CartAddForm cartAddForm) {
        Integer quantity = 1;
        Product product = productMapper.selectByPrimaryKey(cartAddForm.getProductId());

        // 商品是否存在
        if (product == null) {
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }

        // 商品是否正常在售
        if (!product.getStatus().equals(ProductStatusEnum.ON_SALE.getCode())) {
            return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE);
        }

        // 商品库存是否充足
        if (product.getStock() <= 0) {
            return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR);
        }

        // 写入redis
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        String value = opsForHash.get(redisKey, String.valueOf(product.getId()));
        Cart cart;

        if (StringUtil.isNullOrEmpty(value)) {
            // 此前没有添加过此商品，则新增
            cart = new Cart(product.getId(), quantity, cartAddForm.getSelected());
        } else {
            // 此前添加过此商品，则数量加1
            cart = gson.fromJson(value, Cart.class);
            cart.setQuantity(cart.getQuantity() + quantity);
        }

        opsForHash.put(
                redisKey,
                String.valueOf(product.getId()),
                gson.toJson(cart)
        );

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> list(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> entries = opsForHash.entries(redisKey);
        CartVo cartVo = new CartVo();
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        Boolean selectAll = true;
        Integer cartTotalQuantity = 0;
        BigDecimal cartTotalPrice = BigDecimal.ZERO;

        for (Map.Entry<String, String> entry : entries.entrySet()) {
            Integer productId = Integer.valueOf(entry.getKey());
            Cart cart = gson.fromJson(entry.getValue(), Cart.class);

            // TODO 需要优化，使用mysql里的in
            Product product = productMapper.selectByPrimaryKey(productId);
            if (product != null) {
                CartProductVo cartProductVo = new CartProductVo(
                        productId,
                        cart.getQuantity(),
                        product.getName(),
                        product.getSubtitle(),
                        product.getMainImage(),
                        product.getPrice(),
                        product.getStatus(),
                        product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())),
                        product.getStock(),
                        cart.getProductSelected()
                );
                cartProductVoList.add(cartProductVo);

                if (!cart.getProductSelected()) {
                    selectAll = false;
                }

                // 选中的才做累加
                if (cartProductVo.getProductSelected()) {
                    cartTotalPrice = cartTotalPrice.add(cartProductVo.getProductTotalPrice());
                }
            }

            cartTotalQuantity += cart.getQuantity();
        }

        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setSelectedAll(selectAll);
        cartVo.setCartTotalQuantity(cartTotalQuantity);
        cartVo.setCartTotalPrice(cartTotalPrice);

        return ResponseVo.success(cartVo);
    }

    @Override
    public ResponseVo<CartVo> update(Integer uid, Integer productId, CartUpdateForm cartUpdateForm) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        String value = opsForHash.get(redisKey, String.valueOf(productId));

        // 没有该商品，则报错
        if (StringUtil.isNullOrEmpty(value)) {
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }

        // 修改
        Cart cart = gson.fromJson(value, Cart.class);

        if (cartUpdateForm.getQuantity() != null && cartUpdateForm.getQuantity() >= 0) {
            cart.setQuantity(cartUpdateForm.getQuantity());
        }
        if (cartUpdateForm.getSelected() != null) {
            cart.setProductSelected(cartUpdateForm.getSelected());
        }
        opsForHash.put(
                redisKey,
                String.valueOf(productId),
                gson.toJson(cart)
        );

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> delete(Integer uid, Integer productId) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        String value = opsForHash.get(redisKey, String.valueOf(productId));

        // 没有该商品，则报错
        if (StringUtil.isNullOrEmpty(value)) {
            return ResponseVo.error(ResponseEnum.CART_PRODUCT_NOT_EXIST);
        }

        // 修改
        opsForHash.delete(
                redisKey,
                String.valueOf(productId)
        );

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> selectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        List<Cart> cartList = listForCart(uid);
        for (Cart cart : cartList) {
            cart.setProductSelected(true);
            opsForHash.put(
                    redisKey,
                    String.valueOf(cart.getProductId()),
                    gson.toJson(cart)
            );
        }

        return list(uid);
    }

    @Override
    public ResponseVo<CartVo> unSelectAll(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        List<Cart> cartList = listForCart(uid);
        for (Cart cart : cartList) {
            cart.setProductSelected(false);
            opsForHash.put(
                    redisKey,
                    String.valueOf(cart.getProductId()),
                    gson.toJson(cart)
            );
        }

        return list(uid);
    }

    @Override
    public ResponseVo<Integer> sum(Integer uid) {
        List<Cart> cartList = listForCart(uid);
        Integer sum = 0;

        for (Cart cart : cartList) {
            sum += cart.getQuantity();
        }

        return ResponseVo.success(sum);
    }

    /**
     * 将redis查询出的cart转化为list
     * @param uid
     * @return
     */
    public List<Cart> listForCart(Integer uid) {
        HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
        String redisKey = String.format(CART_REDIS_KEY_TEMPLATE, uid);
        Map<String, String> entries = opsForHash.entries(redisKey);

        List<Cart> cartList = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            cartList.add(gson.fromJson(entry.getValue(), Cart.class));
        }

        return cartList;
    }

}
