package com.guoshi.mall.form;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 添加商品到购物车的表单
 */
@Data
public class CartAddForm {

    @NotNull
    private Integer productId;

    private Boolean selected = true;

}
