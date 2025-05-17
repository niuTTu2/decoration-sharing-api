package com.huang.decorationsharingapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String thumbUrl;
    private Long categoryId;
    private String categoryName;
    private UserInfo uploader;
    private Integer views;
    private Integer favorites;
    private List<String> tags;
    private String license;
    private String status;
    private boolean isFavorite;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String avatar;
        private String bio;
    }
}