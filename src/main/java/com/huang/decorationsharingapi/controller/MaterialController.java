package com.huang.decorationsharingapi.controller;

import com.huang.decorationsharingapi.dto.request.MaterialRequest;
import com.huang.decorationsharingapi.dto.response.FavoriteResponse;
import com.huang.decorationsharingapi.dto.response.MaterialDetailResponse;
import com.huang.decorationsharingapi.dto.response.MaterialResponse;
import com.huang.decorationsharingapi.dto.response.PagedResponse;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.service.MaterialService;
import com.huang.decorationsharingapi.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
//处理装修材料本身相关的操作
@RestController
@RequiredArgsConstructor
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialService materialService;
    private final FavoriteRepository favoriteRepository;

    @GetMapping
    public ResponseEntity<PagedResponse<MaterialResponse>> getMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status, // 新增status参数
            @RequestParam(required = false) String keyword, // 新增keyword参数
            Principal principal) {

        // 如果没有指定状态且不是管理员，强制设置为APPROVED
        if (status == null || status.isEmpty()) {
            status = "APPROVED";
        }

        // 获取用户名，用于检查收藏状态
        final String username = principal != null ? principal.getName() : null;

        Page<Material> materials = materialService.getMaterials(
                page, pageSize, categoryId, sort, status, keyword, username);

        List<MaterialResponse> content = materials.getContent().stream()
                .map(material -> convertToMaterialResponse(material, username))
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

    @GetMapping("/{id}")
    public ResponseEntity<MaterialDetailResponse> getMaterialById(@PathVariable Long id, Principal principal) {
        final String username = principal != null ? principal.getName() : null;

        Material material = materialService.getMaterialById(id, username);

        // 非管理员只能查看已审核的素材
        if (username == null && material.getStatus() != Material.Status.APPROVED) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToMaterialDetailResponse(material, username));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<MaterialResponse>> searchMaterials(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Long categoryId,
            Principal principal) {

        // 强制设置状态为APPROVED
        final String status = "APPROVED";
        final String username = principal != null ? principal.getName() : null;

        Page<Material> materials = materialService.searchMaterials(keyword, page, pageSize, categoryId, status);

        List<MaterialResponse> content = materials.getContent().stream()
                .map(material -> convertToMaterialResponse(material, username))
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MaterialResponse> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute MaterialRequest materialRequest,
            Principal principal) {

        final String username = principal.getName();
        Material material = materialService.uploadMaterial(materialRequest, file, username);
        return ResponseEntity.ok(convertToMaterialResponse(material, username));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<FavoriteResponse> toggleFavorite(@PathVariable Long id, Principal principal) {
        boolean isFavorite = materialService.toggleFavorite(id, principal.getName());
        return ResponseEntity.ok(new FavoriteResponse(isFavorite));
    }

    private MaterialResponse convertToMaterialResponse(Material material, String currentUsername) {
        // 检查当前用户是否收藏了该素材
        boolean isFavorite = false;
        if (currentUsername != null) {
            isFavorite = materialService.checkIsFavorite(material.getId(), currentUsername);
        }

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
                .rejectReason(material.getRejectReason())
                .isFavorite(isFavorite) // 正确设置收藏状态
                .createdAt(material.getCreatedAt())
                .build();
    }

    private MaterialDetailResponse convertToMaterialDetailResponse(Material material, String currentUsername) {
        // 检查当前用户是否收藏了该素材
        boolean isFavorite = false;
        if (currentUsername != null) {
            isFavorite = materialService.checkIsFavorite(material.getId(), currentUsername);
        }

        return MaterialDetailResponse.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .imageUrl(material.getImageUrl())
                .thumbUrl(material.getThumbUrl())
                .categoryId(material.getCategory().getId())
                .categoryName(material.getCategory().getName())
                .uploader(convertToUserInfo(material))
                .views(material.getViews())
                .favorites(material.getFavorites())
                .tags(material.getTags())
                .license(material.getLicense())
                .status(material.getStatus().name())
                .isFavorite(isFavorite) // 正确设置收藏状态
                .createdAt(material.getCreatedAt())
                .build();
    }

    private MaterialDetailResponse.UserInfo convertToUserInfo(Material material) {
        return MaterialDetailResponse.UserInfo.builder()
                .id(material.getUser().getId())
                .username(material.getUser().getUsername())
                .avatar(material.getUser().getAvatar())
                .bio(material.getUser().getBio())
                .build();
    }
}