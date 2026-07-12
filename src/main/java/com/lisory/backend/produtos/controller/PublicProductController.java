package com.lisory.backend.produtos.controller;

import com.lisory.backend.produtos.dto.ProductFilter;
import com.lisory.backend.produtos.dto.ProductResponse;
import com.lisory.backend.produtos.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class PublicProductController {

    private final ProductService productService;

    public PublicProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID collectionId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        ProductFilter filter = new ProductFilter();
        filter.setSearch(search);
        filter.setCategoryId(categoryId);
        filter.setCollectionId(collectionId);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setSort(sort);
        filter.setPage(pageable.getPageNumber());
        filter.setSize(pageable.getPageSize());
        return ResponseEntity.ok(productService.findAll(filter));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> findFeatured() {
        return ResponseEntity.ok(productService.findFeatured());
    }

    @GetMapping("/new-arrivals")
    public ResponseEntity<List<ProductResponse>> findNewArrivals() {
        return ResponseEntity.ok(productService.findNewArrivals());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.findBySlug(slug));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<ProductResponse>> findRelated(@PathVariable UUID id,
                                                             @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(productService.findRelated(id, limit));
    }
}
