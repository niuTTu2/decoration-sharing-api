package com.huang.decorationsharingapi.service;

import com.huang.decorationsharingapi.dto.request.MaterialRequest;
import com.huang.decorationsharingapi.entity.Category;
import com.huang.decorationsharingapi.entity.Favorite;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.entity.User;
import com.huang.decorationsharingapi.exception.ResourceNotFoundException;
import com.huang.decorationsharingapi.repository.CategoryRepository;
import com.huang.decorationsharingapi.repository.FavoriteRepository;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor//将所有的final字段注入到构造函数中
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final FileStorageService fileStorageService;

    public Page<Material> getMaterials(int page, int size, Long categoryId, String sort,
                                       String status, String keyword, String username) {
        Sort sortOrder = createSortOrder(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 如果status为空，默认为APPROVED
        if (status == null || status.isEmpty()) {
            status = "APPROVED";
        }

        Specification<Material> spec = createMaterialSpecification(categoryId, status, keyword, username);
        return materialRepository.findAll(spec, pageable);
    }

    public Material getMaterialById(Long id, String username) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));

        // 增加浏览量
        material.setViews(material.getViews() + 1);
        return materialRepository.save(material);
    }

    public Page<Material> searchMaterials(String keyword, int page, int size, Long categoryId, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 如果status为空，默认为APPROVED
        if (status == null || status.isEmpty()) {
            status = "APPROVED";
        }

        Specification<Material> spec = createMaterialSpecification(categoryId, status, keyword, null);
        return materialRepository.findAll(spec, pageable);
    }

    @Transactional
    public Material uploadMaterial(MaterialRequest materialRequest, MultipartFile file, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Category category = categoryRepository.findById(materialRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", materialRequest.getCategoryId()));

        // 保存图片文件
        String imageUrl = fileStorageService.storeFile(file);
        String thumbUrl = fileStorageService.createThumbnail(file);

        Material material = Material.builder()
                .title(materialRequest.getTitle())
                .description(materialRequest.getDescription())
                .imageUrl(imageUrl)
                .thumbUrl(thumbUrl)
                .category(category)
                .user(user)
                .views(0)
                .favorites(0)
                .tags(materialRequest.getTags())
                .license(materialRequest.getLicense())
                .status(Material.Status.PENDING)
                .build();

        return materialRepository.save(material);
    }

    @Transactional
    public boolean toggleFavorite(Long materialId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", materialId));

        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserAndMaterial(user, material);

        // 如果已经收藏，则取消收藏
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            material.setFavorites(material.getFavorites() - 1);
            materialRepository.save(material);
            return false;
        }
        // 否则添加收藏
        else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .material(material)
                    .build();
            favoriteRepository.save(favorite);
            material.setFavorites(material.getFavorites() + 1);
            materialRepository.save(material);
            return true;
        }
    }

    /**
     * 检查用户是否收藏了素材
     */
    public boolean checkIsFavorite(Long materialId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", materialId));

        return favoriteRepository.findByUserAndMaterial(user, material).isPresent();
    }

    public Page<Material> getUserMaterials(String username, int page, int size, String status, Long categoryId, String keyword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Material> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 用户的上传
            predicates.add(cb.equal(root.get("user"), user));

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
                        cb.like(cb.lower(root.get("description")), likePattern)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return materialRepository.findAll(spec, pageable);
    }

    public Page<Material> getUserFavorites(String username, int page, int size, Long categoryId, String keyword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return materialRepository.findUserFavorites(user.getId(), categoryId, keyword, pageable);
    }

    private Sort createSortOrder(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        switch (sort) {
            case "latest":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest":
                return Sort.by(Sort.Direction.ASC, "createdAt");
            case "popular":
                return Sort.by(Sort.Direction.DESC, "views");
            case "name":
                return Sort.by(Sort.Direction.ASC, "title");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    private Specification<Material> createMaterialSpecification(
            Long categoryId, String statusStr, String keyword, String username) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 分类过滤
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // 状态过滤 - 需要使用内部枚举类
            if (statusStr != null && !statusStr.isEmpty()) {
                try {
                    // 正确方式: 使用Material.Status枚举
                    Material.Status status = Material.Status.valueOf(statusStr.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    System.err.println("无效的状态值: " + statusStr + ", 将被忽略");
                    // 默认设为已审核状态
                    predicates.add(cb.equal(root.get("status"), Material.Status.APPROVED));
                }
            }

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchTerm = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), searchTerm),
                        cb.like(cb.lower(root.get("description")), searchTerm)
                ));
            }

            return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}