package com.huang.decorationsharingapi.controller;

import com.huang.decorationsharingapi.dto.request.MaterialRequest;
import com.huang.decorationsharingapi.dto.response.FavoriteResponse;
import com.huang.decorationsharingapi.dto.response.MaterialDetailResponse;
import com.huang.decorationsharingapi.dto.response.MaterialResponse;
import com.huang.decorationsharingapi.dto.response.PagedResponse;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.entity.User;
import com.huang.decorationsharingapi.service.MaterialService;
import com.huang.decorationsharingapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialService materialService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PagedResponse<MaterialResponse>> getMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Authentication authentication) {

        // 检查用户角色
        boolean isAdmin = false;
        // 将username声明为final，确保它不会被修改
        final String username;

        if (authentication != null) {
            username = authentication.getName();
            isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN"));
        } else {
            username = null;
        }

        // 非管理员用户只能查看已审核通过的素材
        String effectiveStatus = status;
        if (!isAdmin && (effectiveStatus == null || effectiveStatus.isEmpty())) {
            effectiveStatus = "APPROVED";
        }

        Page<Material> materials = materialService.getMaterials(
                page, pageSize, categoryId, sort, effectiveStatus, keyword, username);

        // 现在username是effectively final，可以在lambda中使用
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
    public ResponseEntity<MaterialDetailResponse> getMaterialById(
            @PathVariable Long id, Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        boolean isAdmin = false;

        if (authentication != null) {
            isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN"));
        }

        Material material = materialService.getMaterialById(id, username);

        // 检查素材状态，非管理员/素材上传者只能查看已审核通过的素材
        boolean isUploader = username != null && username.equals(material.getUser().getUsername());
        if (!isAdmin && !isUploader && material.getStatus() != Material.Status.APPROVED) {
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
            Authentication authentication) {

        // 将username声明为final
        final String username;
        boolean isAdmin = false;

        if (authentication != null) {
            username = authentication.getName();
            isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN"));
        } else {
            username = null;
        }

        // 非管理员用户默认只搜索已审核通过的素材
        String effectiveStatus = isAdmin ? null : "APPROVED";

        Page<Material> materials = materialService.searchMaterials(
                keyword, page, pageSize, categoryId, effectiveStatus);

        // 使用final或effectively final的username变量
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

        Material material = materialService.uploadMaterial(materialRequest, file, principal.getName());
        return ResponseEntity.ok(convertToMaterialResponse(material, principal.getName()));
    }

    @PostMapping("/{id}/favorite")
    public ResponseEntity<FavoriteResponse> toggleFavorite(@PathVariable Long id, Principal principal) {
        boolean isFavorite = materialService.toggleFavorite(id, principal.getName());
        return ResponseEntity.ok(new FavoriteResponse(isFavorite));
    }

    private MaterialResponse convertToMaterialResponse(Material material, String currentUsername) {
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
                .isFavorite(isFavorite)
                .createdAt(material.getCreatedAt())
                .build();
    }

    private MaterialDetailResponse convertToMaterialDetailResponse(Material material, String currentUsername) {
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
                .isFavorite(isFavorite)
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