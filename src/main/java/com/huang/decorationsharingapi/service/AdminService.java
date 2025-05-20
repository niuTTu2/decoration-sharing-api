package com.huang.decorationsharingapi.service;


import com.huang.decorationsharingapi.dto.request.CategoryRequest;
import com.huang.decorationsharingapi.dto.request.UpdateUserRequest;
import com.huang.decorationsharingapi.dto.response.AdminStatsResponse;
import com.huang.decorationsharingapi.entity.Category;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.entity.User;
import com.huang.decorationsharingapi.exception.ResourceNotFoundException;
import com.huang.decorationsharingapi.repository.CategoryRepository;
import com.huang.decorationsharingapi.repository.MaterialRepository;
import com.huang.decorationsharingapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final CategoryRepository categoryRepository;

    // ========== 用户管理 ==========
    public Page<User> getUsers(int page, int size, String role, String status, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 角色筛选
            if (role != null && !role.isEmpty()) {
                try {
                    User.Role userRole = User.Role.valueOf(role.toUpperCase());
                    predicates.add(cb.equal(root.get("role"), userRole));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的角色值
                }
            }

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                try {
                    User.Status userStatus = User.Status.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), userStatus));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的状态值
                }
            }

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), likePattern),
                        cb.like(cb.lower(root.get("email")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable);
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.getUsername())) {
                throw new IllegalArgumentException("用户名已被使用");
            }
            user.setUsername(updateRequest.getUsername());
        }

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已被使用");
            }
            user.setEmail(updateRequest.getEmail());
        }

        if (updateRequest.getRole() != null) {
            try {
                User.Role role = User.Role.valueOf(updateRequest.getRole().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的角色");
            }
        }

        if (updateRequest.getStatus() != null) {
            try {
                User.Status status = User.Status.valueOf(updateRequest.getStatus().toUpperCase());
                user.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("无效的状态");
            }
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
    }

    // ========== 素材管理 ==========
    public Page<Material> getMaterials(int page, int size, String status, Long categoryId, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Material> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 状态筛选
            if (status != null && !status.isEmpty()) {
                try {
                    Material.Status materialStatus = Material.Status.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), materialStatus));
                } catch (IllegalArgumentException e) {
                    // 忽略无效的状态值
                }
            }

            // 分类筛选
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), likePattern),
                        cb.like(cb.lower(root.get("description")), likePattern),
                        cb.like(cb.lower(root.get("user").get("username")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return materialRepository.findAll(spec, pageable);
    }

    public Page<Material> getPendingMaterials(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return materialRepository.findByStatus(Material.Status.PENDING, pageable);
    }

    @Transactional
    public Material approveMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));

        material.setStatus(Material.Status.APPROVED);
        return materialRepository.save(material);
    }

    @Transactional
    public Material rejectMaterial(Long id, String reason) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));

        material.setStatus(Material.Status.REJECTED);
        material.setRejectReason(reason);
        return materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));

        materialRepository.delete(material);
    }

    // ========== 分类管理 ==========
    @Transactional
    public Category createCategory(CategoryRequest categoryRequest) {
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .description(categoryRequest.getDescription())
                .iconUrl(categoryRequest.getIconUrl())
                .color(categoryRequest.getColor())
                .sort(categoryRequest.getSort())
                .build();

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        if (!category.getName().equals(categoryRequest.getName()) &&
                categoryRepository.existsByName(categoryRequest.getName())) {
            throw new IllegalArgumentException("分类名称已存在");
        }

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setIconUrl(categoryRequest.getIconUrl());
        category.setColor(categoryRequest.getColor());
        category.setSort(categoryRequest.getSort());

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // 检查是否有素材使用此分类
        long count = materialRepository.countByCategory(category);
        if (count > 0) {
            throw new IllegalStateException("该分类下还有" + count + "个素材，无法删除");
        }

        categoryRepository.delete(category);
    }

    /** ========== 统计数据 ==========
    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalMaterials = materialRepository.count();
        long pendingMaterials = materialRepository.countByStatus(Material.Status.PENDING);

        // 计算过去30天的活跃用户数
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long activeUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);

        // 计算用户增长率
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        long lastMonthUsers = userRepository.countByCreatedAtBefore(lastMonth);
        double userGrowthRate = lastMonthUsers == 0 ? 100 : (totalUsers - lastMonthUsers) * 100.0 / lastMonthUsers;

        // 计算素材增长率
        long lastMonthMaterials = materialRepository.countByCreatedAtBefore(lastMonth);
        double materialGrowthRate = lastMonthMaterials == 0 ? 100 : (totalMaterials - lastMonthMaterials) * 100.0 / lastMonthMaterials;

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalMaterials(totalMaterials)
                .pendingMaterials(pendingMaterials)
                .activeUsers(activeUsers)
                .userGrowth(Math.round(userGrowthRate * 10) / 10.0)  // 保留一位小数
                .materialGrowth(Math.round(materialGrowthRate * 10) / 10.0)  // 保留一位小数
                .build();
    }*/
}