package com.guoshi.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.guoshi.mall.dao.OrderItemMapper;
import com.guoshi.mall.dao.OrderMapper;
import com.guoshi.mall.dao.ProductMapper;
import com.guoshi.mall.dao.ShippingMapper;
import com.guoshi.mall.enums.OrderStatusEnum;
import com.guoshi.mall.enums.PaymentTypeEnum;
import com.guoshi.mall.enums.ProductStatusEnum;
import com.guoshi.mall.enums.ResponseEnum;
import com.guoshi.mall.pojo.*;
import com.guoshi.mall.service.ICartService;
import com.guoshi.mall.service.IOrderService;
import com.guoshi.mall.vo.OrderItemVo;
import com.guoshi.mall.vo.OrderVo;
import com.guoshi.mall.vo.ResponseVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private ICartService cartService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    @Transactional // 事务控制
    public ResponseVo<OrderVo> create(Integer uid, Integer shippingId) {
        // 收获地址校验
        Shipping shipping = shippingMapper.selectByUidAndShippingId(uid, shippingId);
        if (shipping == null) {
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }

        // 获取购物车，校验是否有商品，是否在售，库存是否充足
        List<Cart> cartList = cartService.listForCart(uid).stream()
                .filter(Cart::getProductSelected)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST);
        }

        Set<Integer> productIds = cartList.stream()
                .map(Cart::getProductId)
                .collect(Collectors.toSet());
        List<Product> productList = productMapper.selectByProductIdSet(productIds);
        Map<Integer, Product> map = productList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        List<OrderItem> orderItemList = new ArrayList<>();
        Long orderNo = generateOrderNo();

        for (Cart cart : cartList) {
            Product product = map.get(cart.getProductId());
            if (product == null) {
                return ResponseVo.error(ResponseEnum.PRODUCT_NOT_EXIST, "商品不存在. productId = " + cart.getProductId());
            }

            if (!ProductStatusEnum.ON_SALE.getCode().equals(product.getStatus())) {
                return ResponseVo.error(ResponseEnum.PRODUCT_OFF_SALE_OR_DELETE, "商品不是在售状态. " + product.getName());
            }

            if (product.getStock() < cart.getQuantity()) {
                return ResponseVo.error(ResponseEnum.PRODUCT_STOCK_ERROR, "库存不足. " + product.getName());
            }

            OrderItem orderItem = buildOrderItem(uid, orderNo, cart.getQuantity(), product);
            orderItemList.add(orderItem);

            // 减库存
            product.setStock(product.getStock() - cart.getQuantity());
            int row = productMapper.updateByPrimaryKeySelective(product);
            if (row <= 0) {
                return ResponseVo.error(ResponseEnum.ERROR);
            }
        }

        // 计算总价，只计算选中的商品
        // 生成订单，入库：order和order_item表，事务
        Order order = buildOrder(uid, orderNo, shippingId, orderItemList);
        int row = orderMapper.insertSelective(order);
        if (row <= 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        int batchRow = orderItemMapper.batchInsert(orderItemList);
        if (batchRow <= 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        // 更新购物车
        for (Cart cart : cartList) {
            cartService.delete(uid, cart.getProductId());
        }

        // 构造orderVo对象
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);

        return ResponseVo.success(orderVo);
    }

    @Override
    public ResponseVo<PageInfo> list(Integer uid, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUid(uid);

        Set<Long> orderNoSet = orderList.stream()
                .map(Order::getOrderNo)
                .collect(Collectors.toSet());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        Map<Long, List<OrderItem>> orderItemMap = orderItemList.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderNo));

        Set<Integer> shippingIdSet = orderList.stream()
                .map(Order::getShippingId)
                .collect(Collectors.toSet());
        List<Shipping> shippingList = shippingMapper.selectByIdSet(shippingIdSet);
        Map<Integer, Shipping> shippingMap = shippingList.stream()
                .collect(Collectors.toMap(Shipping::getId, shipping -> shipping));

        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order order : orderList) {
            OrderVo orderVo = buildOrderVo(
                    order,
                    orderItemMap.get(order.getOrderNo()),
                    shippingMap.get(order.getShippingId())
            );
            orderVoList.add(orderVo);
        }
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ResponseVo.success(pageInfo);
    }

    @Override
    public ResponseVo<OrderVo> detail(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(uid)) {
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }

        Set<Long> orderNoSet = new HashSet<>();
        orderNoSet.add(order.getOrderNo());
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoSet(orderNoSet);
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        OrderVo orderVo = buildOrderVo(order, orderItemList, shipping);

        return ResponseVo.success(orderVo);
    }

    @Override
    public ResponseVo cancel(Integer uid, Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(uid)) {
            return ResponseVo.error(ResponseEnum.ORDER_NOT_EXIST);
        }

        // 只有未付款的订单才能取消
        if (!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            return ResponseVo.error(ResponseEnum.ORDER_STATUS_ERROR);
        }

        order.setStatus(OrderStatusEnum.CANCELED.getCode());
        order.setCloseTime(new Date());
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row <= 0) {
            return ResponseVo.error(ResponseEnum.ERROR);
        }

        return ResponseVo.success();
    }

    @Override
    public void paid(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);

        if (order == null) {
            throw new RuntimeException(ResponseEnum.ORDER_NOT_EXIST.getDesc() + "订单id: " + orderNo);
        }

        // 只有未付款的订单才能支付
        if (!order.getStatus().equals(OrderStatusEnum.NO_PAY.getCode())) {
            throw new RuntimeException(ResponseEnum.ORDER_STATUS_ERROR.getDesc() + "订单id: " + orderNo);
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPaymentTime(new Date());
        int row = orderMapper.updateByPrimaryKeySelective(order);
        if (row <= 0) {
            throw new RuntimeException("将订单更新为已支付状态失败，订单id：" + orderNo);
        }
    }

    /**
     * 企业级：分布式唯一id
     *
     * @return
     */
    private Long generateOrderNo() {
        return System.currentTimeMillis() + new Random().nextInt(999);
    }

    private OrderItem buildOrderItem(Integer uid, Long orderNo, Integer quantity, Product product) {
        OrderItem orderItem = new OrderItem();
        orderItem.setUserId(uid);
        orderItem.setOrderNo(orderNo);
        orderItem.setProductId(product.getId());
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getMainImage());
        orderItem.setCurrentUnitPrice(product.getPrice());
        orderItem.setQuantity(quantity);
        orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

        return orderItem;
    }

    private Order buildOrder(
            Integer uid,
            Long orderNo,
            Integer shippingId,
            List<OrderItem> orderItemList
    ) {
        BigDecimal payment = orderItemList.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(uid);
        order.setOrderNo(orderNo);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(PaymentTypeEnum.PAY_ONLINE.getCode());
        order.setPostage(10);
        order.setStatus(OrderStatusEnum.NO_PAY.getCode());

        return order;
    }

    private OrderVo buildOrderVo(Order order, List<OrderItem> orderItemList, Shipping shipping) {
        OrderVo orderVo = new OrderVo();
        BeanUtils.copyProperties(order, orderVo);

        List<OrderItemVo> orderItemVoList = orderItemList.stream()
                .map(e -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    BeanUtils.copyProperties(e, orderItemVo);
                    return orderItemVo;
                })
                .collect(Collectors.toList());

        if (shipping != null) {
            orderVo.setShippingId(shipping.getId());
            orderVo.setShippingVo(shipping);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

}
