package com.lisory.backend.produtos.services;

import com.lisory.backend.exception.BusinessException;
import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.produtos.dto.CollectionRequest;
import com.lisory.backend.produtos.dto.CollectionResponse;
import com.lisory.backend.produtos.entity.Collection;
import com.lisory.backend.produtos.repository.CollectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;

    public CollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Transactional
    public CollectionResponse create(CollectionRequest request) {
        String slug = generateSlug(request.name());

        if (collectionRepository.existsBySlug(slug)) {
            throw new BusinessException("Collection slug already exists: " + slug);
        }

        Collection collection = new Collection();
        collection.setName(request.name());
        collection.setSlug(slug);
        collection.setDescription(request.description());
        collection.setActive(request.active() != null ? request.active() : true);

        return toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public CollectionResponse update(UUID id, CollectionRequest request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id));

        String slug = generateSlug(request.name());
        if (!collection.getSlug().equals(slug) && collectionRepository.existsBySlug(slug)) {
            throw new BusinessException("Collection slug already exists: " + slug);
        }

        collection.setName(request.name());
        collection.setSlug(slug);
        collection.setDescription(request.description());
        collection.setActive(request.active() != null ? request.active() : collection.getActive());

        return toResponse(collectionRepository.save(collection));
    }

    @Transactional
    public void delete(UUID id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id));
        collectionRepository.delete(collection);
    }

    public CollectionResponse findById(UUID id) {
        return toResponse(collectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "id", id)));
    }

    public CollectionResponse findBySlug(String slug) {
        return toResponse(collectionRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", "slug", slug)));
    }

    public List<CollectionResponse> findAll() {
        return collectionRepository.findByActiveTrue().stream()
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

    private CollectionResponse toResponse(Collection collection) {
        return new CollectionResponse(
                collection.getId(),
                collection.getName(),
                collection.getSlug(),
                collection.getDescription(),
                collection.getActive(),
                collection.getCreatedAt()
        );
    }
}
