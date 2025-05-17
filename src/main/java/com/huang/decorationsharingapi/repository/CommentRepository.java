package com.huang.decorationsharingapi.repository;


import com.huang.decorationsharingapi.entity.Comment;
import com.huang.decorationsharingapi.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByMaterialAndParentIsNull(Material material, Pageable pageable);

    List<Comment> findByParentId(Long parentId);
}