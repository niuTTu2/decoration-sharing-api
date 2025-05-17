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
public class MaterialResponse {
    private Long id;
    private String title;
    private String imageUrl;
    private String thumbUrl;
    private Long categoryId;
    private String categoryName;
    private String uploaderName;
    private String uploaderAvatar;
    private Integer views;
    private Integer favorites;
    private List<String> tags;
    private String status;
    private String rejectReason;
    private boolean isFavorite;
    private LocalDateTime createdAt;
}