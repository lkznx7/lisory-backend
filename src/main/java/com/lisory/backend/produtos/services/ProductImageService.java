package com.lisory.backend.produtos.services;

import com.lisory.backend.exception.ResourceNotFoundException;
import com.lisory.backend.produtos.dto.ProductImageResponse;
import com.lisory.backend.produtos.entity.Product;
import com.lisory.backend.produtos.entity.ProductImage;
import com.lisory.backend.produtos.repository.ProductImageRepository;
import com.lisory.backend.produtos.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageService(ProductImageRepository productImageRepository,
                               ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductImageResponse addImage(UUID productId, String imageUrl, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (isPrimary) {
            product.getImages().stream()
                    .filter(img -> img.getPrimary())
                    .forEach(img -> img.setPrimary(false));
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setPrimary(isPrimary);

        return toResponse(productImageRepository.save(image));
    }

    @Transactional
    public void deleteImage(UUID imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));
        productImageRepository.delete(image);
    }

    @Transactional
    public void setPrimaryImage(UUID imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", imageId));

        image.getProduct().getImages().stream()
                .filter(img -> img.getPrimary())
                .forEach(img -> img.setPrimary(false));

        image.setPrimary(true);
        productImageRepository.save(image);
    }

    private ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getPrimary()
        );
    }
}
