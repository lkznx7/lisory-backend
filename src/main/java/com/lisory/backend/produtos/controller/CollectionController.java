package com.lisory.backend.produtos.controller;

import com.lisory.backend.produtos.dto.CollectionRequest;
import com.lisory.backend.produtos.dto.CollectionResponse;
import com.lisory.backend.produtos.services.CollectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public ResponseEntity<CollectionResponse> create(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(collectionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CollectionResponse>> findAll() {
        return ResponseEntity.ok(collectionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionResponse> update(@PathVariable UUID id, @Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(collectionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
