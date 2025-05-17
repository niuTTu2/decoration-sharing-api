package com.huang.decorationsharingapi.repository;


import com.huang.decorationsharingapi.entity.Category;
import com.huang.decorationsharingapi.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {
    Page<Material> findByStatus(Material.Status status, Pageable pageable);

    long countByCategory(Category category);

    long countByStatus(Material.Status status);

    long countByCreatedAtBefore(LocalDateTime date);

    @Query("SELECT m FROM Material m JOIN Favorite f ON m.id = f.material.id " +
            "WHERE f.user.id = :userId " +
            "AND (:categoryId IS NULL OR m.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "    OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Material> findUserFavorites(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable);
}