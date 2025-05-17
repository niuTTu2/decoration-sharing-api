package com.huang.decorationsharingapi.controller;


import com.huang.decorationsharingapi.dto.request.CategoryRequest;
import com.huang.decorationsharingapi.dto.request.ReviewRequest;
import com.huang.decorationsharingapi.dto.request.UpdateUserRequest;
import com.huang.decorationsharingapi.dto.response.*;
import com.huang.decorationsharingapi.entity.Category;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.entity.User;
import com.huang.decorationsharingapi.service.AdminService;
import com.huang.decorationsharingapi.service.CategoryService;
import com.huang.decorationsharingapi.service.MaterialService;
import com.huang.decorationsharingapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final MaterialService materialService;
    private final CategoryService categoryService;
    private final AdminService adminService;

    // ========== 用户管理 ==========
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        Page<User> users = adminService.getUsers(page, pageSize, role, status, keyword);

        List<UserResponse> content = users.getContent().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PagedResponse<>(
                content,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        ));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest updateRequest) {

        User user = adminService.updateUser(id, updateRequest);
        return ResponseEntity.ok(convertToUserResponse(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("用户已删除"));
    }

    // ========== 素材管理 ==========
    @GetMapping("/materials")
    public ResponseEntity<PagedResponse<MaterialResponse>> getMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {

        Page<Material> materials = adminService.getMaterials(page, pageSize, status, categoryId, keyword);

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

    @GetMapping("/materials/pending")
    public ResponseEntity<PagedResponse<MaterialResponse>> getPendingMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<Material> materials = adminService.getPendingMaterials(page, pageSize);

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

    @PutMapping("/materials/{id}/approve")
    public ResponseEntity<MaterialResponse> approveMaterial(@PathVariable Long id) {
        Material material = adminService.approveMaterial(id);
        return ResponseEntity.ok(convertToMaterialResponse(material));
    }

    @PutMapping("/materials/{id}/reject")
    public ResponseEntity<MaterialResponse> rejectMaterial(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest reviewRequest) {

        Material material = adminService.rejectMaterial(id, reviewRequest.getReason());
        return ResponseEntity.ok(convertToMaterialResponse(material));
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<MessageResponse> deleteMaterial(@PathVariable Long id) {
        adminService.deleteMaterial(id);
        return ResponseEntity.ok(new MessageResponse("素材已删除"));
    }

    // ========== 分类管理 ==========
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category category = adminService.createCategory(categoryRequest);
        return ResponseEntity.ok(convertToCategoryResponse(category));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        Category category = adminService.updateCategory(id, categoryRequest);
        return ResponseEntity.ok(convertToCategoryResponse(category));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<MessageResponse> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(new MessageResponse("分类已删除"));
    }

    // ========== 统计数据 ==========
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ========== 辅助方法 ==========
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
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
                .rejectReason(material.getRejectReason())
                .createdAt(material.getCreatedAt())
                .build();
    }

    private CategoryResponse convertToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .iconUrl(category.getIconUrl())
                .color(category.getColor())
                .sort(category.getSort())
                .createdAt(category.getCreatedAt())
                .build();
    }
}