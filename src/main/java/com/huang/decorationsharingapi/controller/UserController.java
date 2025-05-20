package com.huang.decorationsharingapi.controller;


import com.huang.decorationsharingapi.dto.response.MaterialResponse;
import com.huang.decorationsharingapi.dto.response.PagedResponse;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
//处理普通用户相关的操作
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final MaterialService materialService;

    @GetMapping("/materials")
    public ResponseEntity<PagedResponse<MaterialResponse>> getUserMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Principal principal) {

        Page<Material> materials = materialService.getUserMaterials(
                principal.getName(), page, pageSize, status, categoryId, keyword);

        List<MaterialResponse> content = materials.getContent().stream()
                .map(this::convertToMaterialResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PagedResponse<>(
                content,
                materials.getNumber(),
                materials.getSize(),
                materials.getTotalElements(),
                materials.getTotalPages(),
                materials.isLast()
        ));
    }

    @GetMapping("/favorites")
    public ResponseEntity<PagedResponse<MaterialResponse>> getUserFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            Principal principal) {

        Page<Material> materials = materialService.getUserFavorites(
                principal.getName(), page, pageSize, categoryId, keyword);

        List<MaterialResponse> content = materials.getContent().stream()
                .map(this::convertToMaterialResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PagedResponse<>(
                content,
                materials.getNumber(),
                materials.getSize(),
                materials.getTotalElements(),
                materials.getTotalPages(),
                materials.isLast()
        ));
    }

    private MaterialResponse convertToMaterialResponse(Material material) {
        return MaterialResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .imageUrl(material.getImageUrl())
                .thumbUrl(material.getThumbUrl())
                .categoryId(material.getCategory().getId())
                .categoryName(material.getCategory().getName())
                .uploaderName(material.getUser().getUsername())
                .uploaderAvatar(material.getUser().getAvatar())
                .views(material.getViews())
                .favorites(material.getFavorites())
                .tags(material.getTags())
                .status(material.getStatus().name())
                .isFavorite(true) // 收藏列表中的项目一定是收藏的
                .createdAt(material.getCreatedAt())
                .build();
    }
}