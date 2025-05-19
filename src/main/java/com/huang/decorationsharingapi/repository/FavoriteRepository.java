package com.huang.decorationsharingapi.repository;


import com.huang.decorationsharingapi.entity.Favorite;
import com.huang.decorationsharingapi.entity.Material;
import com.huang.decorationsharingapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndMaterial(User user, Material material);

    boolean existsByUserAndMaterial(User user, Material material);
    // 在 FavoriteRepository 接口中添加
    long countByUser(User user);
}