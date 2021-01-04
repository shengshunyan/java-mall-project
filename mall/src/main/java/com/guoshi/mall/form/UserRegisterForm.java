package com.guoshi.mall.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserRegisterForm {

    // @NotEmpty 用于集合
    // @NotNull
    @NotBlank(message = "用户名不能为空") // String非空校验
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

}
