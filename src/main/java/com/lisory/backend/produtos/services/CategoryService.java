package com.lisory.backend.produtos.services;

import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.produtos.dto.CategoryRequest;
import com.lisory.backend.produtos.dto.CategoryResponse;
import com.lisory.backend.produtos.entity.Category;
import com.lisory.backend.produtos.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String slug = generateSlug(request.name());

        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Category slug already exists: " + slug);
        }

        Category category = new Category();
        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setActive(request.active() != null ? request.active() : true);

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        String slug = generateSlug(request.name());
        if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
            throw new BusinessException("Category slug already exists: " + slug);
        }

        category.setName(request.name());
        category.setSlug(slug);
        category.setDescription(request.description());
        category.setActive(request.active() != null ? request.active() : category.getActive());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }

    public CategoryResponse findById(UUID id) {
        return toResponse(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id)));
    }

    public CategoryResponse findBySlug(String slug) {
        return toResponse(categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug)));
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getActive(),
                category.getCreatedAt()
        );
    }
}
