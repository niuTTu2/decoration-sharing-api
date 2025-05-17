package com.huang.decorationsharingapi.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
public class UpdateProfileRequest {
    @Size(min = 3, max = 20, message = "用户名长度应在3-20个字符之间")
    private String username;

    @Email(message = "请输入有效的邮箱地址")
    private String email;

    private String avatar;

    @Size(max = 500, message = "个人简介不能超过500个字符")
    private String bio;
}