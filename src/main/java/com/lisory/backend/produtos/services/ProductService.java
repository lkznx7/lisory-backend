package com.lisory.backend.produtos.services;

import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.produtos.dto.ProductFilter;
import com.lisory.backend.produtos.dto.ProductImageResponse;
import com.lisory.backend.produtos.dto.ProductRequest;
import com.lisory.backend.produtos.dto.ProductResponse;
import com.lisory.backend.produtos.entity.Category;
import com.lisory.backend.produtos.entity.Collection;
import com.lisory.backend.produtos.entity.Product;
import com.lisory.backend.produtos.entity.ProductImage;
import com.lisory.backend.produtos.repository.CategoryRepository;
import com.lisory.backend.produtos.repository.CollectionRepository;
import com.lisory.backend.produtos.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          CollectionRepository collectionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.collectionRepository = collectionRepository;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String slug = generateSlug(request.name());

        if (productRepository.existsBySlug(slug)) {
            throw new BusinessException("Product slug already exists: " + slug);
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSlug(slug);
        product.setDescription(request.description());
        product.setSku(request.sku());
        product.setPrice(request.price());
        product.setPromotionalPrice(request.promotionalPrice());
        product.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        product.setActive(request.active() != null ? request.active() : true);
        product.setFeatured(request.featured() != null ? request.featured() : false);
        product.setWeight(request.weight());
        product.setHeight(request.height());
        product.setWidth(request.width());
        product.setLength(request.length());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
            product.setCategory(category);
        }

        if (request.collectionId() != null) {
            Collection collection = collectionRepository.findById(request.collectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", request.collectionId()));
            product.setCollection(collection);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        String slug = generateSlug(request.name());
        if (!product.getSlug().equals(slug) && productRepository.existsBySlug(slug)) {
            throw new BusinessException("Product slug already exists: " + slug);
        }

        product.setName(request.name());
        product.setSlug(slug);
        product.setDescription(request.description());
        product.setSku(request.sku());
        product.setPrice(request.price());
        product.setPromotionalPrice(request.promotionalPrice());
        product.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : product.getStockQuantity());
        product.setActive(request.active() != null ? request.active() : product.getActive());
        product.setFeatured(request.featured() != null ? request.featured() : product.getFeatured());
        product.setWeight(request.weight());
        product.setHeight(request.height());
        product.setWidth(request.width());
        product.setLength(request.length());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        if (request.collectionId() != null) {
            Collection collection = collectionRepository.findById(request.collectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", request.collectionId()));
            product.setCollection(collection);
        } else {
            product.setCollection(null);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return toResponse(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id)));
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {
        return toResponse(productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug)));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(ProductFilter filter) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(filter.getDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC,
                filter.getSort() != null ? filter.getSort() : "createdAt"
        );
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Page<Product> products;

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            products = productRepository.findByNameContainingIgnoreCaseAndActiveTrue(filter.getSearch(), pageable);
        } else if (filter.getCategoryId() != null) {
            products = productRepository.findByCategoryIdAndActiveTrue(filter.getCategoryId(), pageable);
        } else if (filter.getCollectionId() != null) {
            products = productRepository.findByCollectionIdAndActiveTrue(filter.getCollectionId(), pageable);
        } else if (Boolean.TRUE.equals(filter.getFeatured())) {
            products = productRepository.findByFeaturedTrueAndActiveTrue(pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }

        if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
            List<Product> filtered = products.getContent().stream()
                    .filter(p -> {
                        boolean meetsMin = filter.getMinPrice() == null || p.getPrice().compareTo(filter.getMinPrice()) >= 0;
                        boolean meetsMax = filter.getMaxPrice() == null || p.getPrice().compareTo(filter.getMaxPrice()) <= 0;
                        return meetsMin && meetsMax;
                    })
                    .toList();
            products = new PageImpl<>(filtered, pageable, products.getTotalElements());
        }

        return products.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findRelated(UUID productId, int limit) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getCategory() == null) {
            return List.of();
        }

        return productRepository.findByCategoryIdAndActiveTrue(product.getCategory().getId(), PageRequest.of(0, limit))
                .stream()
                .filter(p -> !p.getId().equals(productId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findFeatured() {
        return productRepository.findByFeaturedTrueAndActiveTrue(PageRequest.of(0, 50))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findNewArrivals() {
        return productRepository.findTop12ByActiveTrueOrderByCreatedAtDesc()
                .stream()
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

    private ProductResponse toResponse(Product product) {
        List<ProductImageResponse> images = product.getImages().stream()
                .map(img -> new ProductImageResponse(img.getId(), img.getImageUrl(), img.getPrimary()))
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getSku(),
                product.getPrice(),
                product.getPromotionalPrice(),
                product.getStockQuantity(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCollection() != null ? product.getCollection().getId() : null,
                product.getCollection() != null ? product.getCollection().getName() : null,
                product.getActive(),
                product.getFeatured(),
                product.getWeight(),
                product.getHeight(),
                product.getWidth(),
                product.getLength(),
                images,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
