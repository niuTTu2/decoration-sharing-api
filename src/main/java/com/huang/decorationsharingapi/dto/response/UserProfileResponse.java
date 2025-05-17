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
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private String role;
    private String status;
    private Long uploadCount;
    private Long favoriteCount;
    private Long followersCount;
    private LocalDateTime createdAt;
}