package com.huang.decorationsharingapi.service;


import com.huang.decorationsharingapi.entity.Category;
import com.huang.decorationsharingapi.exception.ResourceNotFoundException;
import com.huang.decorationsharingapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
//分类
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "sort"));
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}