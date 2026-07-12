package com.lisory.backend.config;

import com.lisory.backend.auth.entity.AuthEntity;
import com.lisory.backend.auth.entity.ROLES;
import com.lisory.backend.auth.repository.AuthRepository;
import com.lisory.backend.cupons.entity.Coupon;
import com.lisory.backend.cupons.repository.CouponRepository;
import com.lisory.backend.produtos.entity.Category;
import com.lisory.backend.produtos.entity.Collection;
import com.lisory.backend.produtos.entity.Product;
import com.lisory.backend.produtos.entity.ProductImage;
import com.lisory.backend.produtos.repository.CategoryRepository;
import com.lisory.backend.produtos.repository.CollectionRepository;
import com.lisory.backend.produtos.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "data.initializer.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public DataInitializer(AuthRepository authRepository,
                           PasswordEncoder passwordEncoder,
                           CategoryRepository categoryRepository,
                           CollectionRepository collectionRepository,
                           ProductRepository productRepository,
                           CouponRepository couponRepository) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepository = categoryRepository;
        this.collectionRepository = collectionRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    @Override
    public void run(String... args) {
        if (authRepository.count() > 0) {
            return;
        }

        createAdmin();
        createCategories();
        createCollections();
        createProducts();
        createCoupons();
    }

    private void createAdmin() {
        AuthEntity admin = new AuthEntity("admin@lisory.com", passwordEncoder.encode("admin123"));
        admin.setRole(ROLES.ADMIN);
        admin.setActive(true);
        authRepository.save(admin);
    }

    private void createCategories() {
        createCategory("Scoop", "scoop", "Caixas surpresa com acessorios selectionados especialmente para voce");
        createCategory("Dourados", "dourados", "Acessorios dourados com garantia de 6 meses");
        createCategory("Prata", "prata", "Acessorios de prata com garantia de 1 ano");
    }

    private void createCategory(String name, String slug, String description) {
        Category cat = new Category();
        cat.setName(name);
        cat.setSlug(slug);
        cat.setDescription(description);
        cat.setActive(true);
        categoryRepository.save(cat);
    }

    private void createCollections() {
        createCollection("Verao 2026", "verao-2026", "Colecao de verao com pecas leves e modernas");
        createCollection("Classica", "classica", "Pecas timeless que nunca saem de moda");
    }

    private void createCollection(String name, String slug, String description) {
        Collection col = new Collection();
        col.setName(name);
        col.setSlug(slug);
        col.setDescription(description);
        col.setActive(true);
        collectionRepository.save(col);
    }

    private void createProducts() {
        Category scoop = categoryRepository.findBySlug("scoop").orElse(null);
        Collection verao = collectionRepository.findBySlug("verao-2026").orElse(null);
        Collection classica = collectionRepository.findBySlug("classica").orElse(null);

        createProduct("Primeira Surpresa", "primeira-surpresa",
                "Uma selecao de 4 acessorios delicados perfeitos para quem quer comecar a explorar o universo das joias.",
                new BigDecimal("89.00"), null, scoop, verao, true, false, "/images/scoop-1.jpg");

        createProduct("Brilho em Dobro", "brilho-em-dobro",
                "6 acessorios incriveis que combinam perfeitamente. Uma experiencia completa de transformacao e estilo.",
                new BigDecimal("149.00"), null, scoop, verao, true, true, "/images/scoop-2.jpg");

        createProduct("Colecao de Sonhos", "colecao-de-sonhos",
                "9 pecas exclusivas que contam uma historia. Do design a apresentacao, cada detalhe foi pensado para surpreender.",
                new BigDecimal("259.00"), new BigDecimal("219.00"), scoop, classica, true, false, "/images/scoop-3.jpg");

        createProduct("Experiencia Premium", "experiencia-premium",
                "12 acessorios premium com embalagem de luxo. A experiencia definitiva para quem merece o melhor.",
                new BigDecimal("349.00"), null, scoop, classica, true, false, "/images/scoop-4.jpg");

        createProduct("ANEL ETERNO", "anel-eterno",
                "Um anel delicado banido a ouro com design minimalista. Perfeito para o dia a dia.",
                new BigDecimal("79.00"), new BigDecimal("59.00"), null, verao, true, true, "/images/scoop-1.jpg");

        createProduct("COLAR MARISOL", "colar-marisol",
                "Colar feminino com pingente de concha banido a ouro. Inspirado no mar e na natureza.",
                new BigDecimal("129.00"), null, null, classica, true, false, "/images/scoop-2.jpg");

        createProduct("BRINCOS LUNA", "brincos-luna",
                "Brincos pingentes com formato de lua crescente. Elegancia e sofisticacao em cada detalhe.",
                new BigDecimal("99.00"), new BigDecimal("79.00"), null, verao, true, false, "/images/scoop-3.jpg");

        createProduct("PULSEIRA CHIADO", "pulseira-chiado",
                "Pulseira feminina com pingentes dourados. Combina com todas as ocasioes.",
                new BigDecimal("119.00"), null, null, classica, true, false, "/images/scoop-4.jpg");
    }

    private void createProduct(String name, String slug, String description,
                               BigDecimal price, BigDecimal promotionalPrice,
                               Category category, Collection collection,
                               boolean active, boolean featured, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setSlug(slug);
        product.setDescription(description);
        product.setSku("SKU-" + slug.toUpperCase());
        product.setPrice(price);
        product.setPromotionalPrice(promotionalPrice);
        product.setStockQuantity(100);
        product.setCategory(category);
        product.setCollection(collection);
        product.setActive(active);
        product.setFeatured(featured);

        ProductImage image = new ProductImage();
        image.setImageUrl(imageUrl);
        image.setPrimary(true);
        image.setProduct(product);
        product.getImages().add(image);

        productRepository.save(product);
    }

    private void createCoupons() {
        Coupon coupon = new Coupon();
        coupon.setCode("LISORY10");
        coupon.setDiscountType("PERCENTAGE");
        coupon.setDiscountValue(new BigDecimal("10"));
        coupon.setMinOrderValue(new BigDecimal("0.01"));
        coupon.setMaxUses(1000);
        coupon.setUsedCount(0);
        coupon.setMaxUsesPerCustomer(1);
        coupon.setExpiresAt(LocalDateTime.now().plusYears(1));
        coupon.setActive(true);
        couponRepository.save(coupon);
    }
}
