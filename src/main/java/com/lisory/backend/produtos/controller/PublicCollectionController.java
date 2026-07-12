package com.lisory.backend.produtos.controller;

import com.lisory.backend.produtos.dto.CollectionResponse;
import com.lisory.backend.produtos.services.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collections")
public class PublicCollectionController {

    private final CollectionService collectionService;

    public PublicCollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> findAll() {
        return ResponseEntity.ok(collectionService.findAll());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<CollectionResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(collectionService.findBySlug(slug));
    }
}
