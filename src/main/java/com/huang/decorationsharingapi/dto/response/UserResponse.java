package com.huang.decorationsharingapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private String role;
    private String status;
    private Integer materialCount; // 添加素材数量
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt; // 添加最后登录时间
}