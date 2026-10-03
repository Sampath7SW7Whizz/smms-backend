package com.smms.backend.service;

import com.smms.backend.model.Product;
import com.smms.backend.model.User;
import com.smms.backend.repository.ProductRepository;
import com.smms.backend.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Cacheable(value = "products", key = "T(com.smms.backend.security.AuthContext).getCurrentUser().id")
    public List<Product> getAllProducts() {
        User user = AuthContext.getCurrentUser();
        if (user == null)
            return new ArrayList<>();
        return productRepository.findByUser(user);
    }

    @org.springframework.transaction.annotation.Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void initializeDefaultsForCurrentUser() {
        User user = AuthContext.getCurrentUser();
        if (user == null)
            return;
        initializeDefaultProducts(user);
    }

    public Optional<Product> getProductById(Long id) {
        User user = AuthContext.getCurrentUser();
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product p = product.get();
            if (p.getUser() == null || (user != null && p.getUser().getId().equals(user.getId()))) {
                return product;
            }
        }
        return Optional.empty();
    }

    public Optional<Product> getProductByItemCode(String itemCode) {
        User user = AuthContext.getCurrentUser();
        if (user == null || itemCode == null || itemCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return productRepository.findByUserAndItemCodeIgnoreCase(user, itemCode.trim());
    }

    public List<Product> searchProducts(String query, int limit) {
        User user = AuthContext.getCurrentUser();
        if (user == null || query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        int safeLimit = Math.min(Math.max(limit, 1), 20);
        return productRepository.searchByUserAndQuery(user, query.trim(), PageRequest.of(0, safeLimit));
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product saveProduct(Product product) {
        User user = AuthContext.getCurrentUser();
        boolean isNew = product.getId() == null;
        product.setUser(user);

        if (isNew) {
            product.setDefault(false);
            if (product.getItemCode() != null && !product.getItemCode().trim().isEmpty()) {
                Optional<Product> existing = productRepository.findByUserAndItemCode(user,
                        product.getItemCode().trim());
                if (existing.isPresent()) {
                    throw new IllegalArgumentException("A product with this code already exists for your account");
                }
            }
        }

        Product savedProduct = productRepository.save(product);

        if (isNew) {
            auditLogService.log("PRODUCT_ADDED", "Product", savedProduct.getId(),
                    String.format("Added product: %s (Code: %s)", savedProduct.getItemName(),
                            savedProduct.getItemCode()));
        } else {
            auditLogService.log("PRODUCT_UPDATED", "Product", savedProduct.getId(),
                    String.format("Updated product: %s", savedProduct.getItemName()));
        }

        return savedProduct;
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        User user = AuthContext.getCurrentUser();
        productRepository.findByUserAndId(user, id).ifPresent(p -> {
            auditLogService.log("PRODUCT_DELETED", "Product", p.getId(),
                    String.format("Deleted product: %s (Code: %s)", p.getItemName(), p.getItemCode()));
            productRepository.delete(p);
        });
    }

    @org.springframework.transaction.annotation.Transactional
    @CacheEvict(value = "products", allEntries = true)
    public List<Product> saveAllProducts(List<Product> products) {
        User user = AuthContext.getCurrentUser();

        List<Product> existingProducts = productRepository.findByUser(user);
        Set<String> existingCodes = existingProducts.stream()
                .map(p -> p.getItemCode() != null ? p.getItemCode().toLowerCase().trim() : "")
                .collect(Collectors.toSet());

        List<Product> toSave = new ArrayList<>();
        for (Product p : products) {
            String code = p.getItemCode() != null ? p.getItemCode().toLowerCase().trim() : "";
            if (!code.isEmpty() && existingCodes.contains(code)) {
                continue;
            }
            p.setUser(user);
            p.setDefault(false);
            toSave.add(p);
        }

        if (toSave.isEmpty())
            return new ArrayList<>();

        List<Product> savedProducts = productRepository.saveAll(toSave);

        auditLogService.log("BULK_PRODUCT_ADDED", "Product", null,
                String.format("Bulk added %d products (skipped %d duplicates)", savedProducts.size(),
                        products.size() - savedProducts.size()));

        return savedProducts;
    }

    @org.springframework.transaction.annotation.Transactional
    public void initializeDefaultProducts(User user) {
        List<Product> templates = productRepository.findByUserIsNullAndIsDefaultTrue();
        if (templates.isEmpty())
            return;

        // Deduplicate templates by itemCode (case-insensitive) to prevent internal clashes
        Map<String, Product> uniqueTemplates = new LinkedHashMap<>();
        for (Product t : templates) {
            String code = (t.getItemCode() != null) ? t.getItemCode().trim().toLowerCase() : "";
            if (!code.isEmpty() && !uniqueTemplates.containsKey(code)) {
                uniqueTemplates.put(code, t);
            } else if (code.isEmpty()) {
                // If code is empty, we still allow it but it might cause other issues later. 
                // For now, only deduplicate non-empty codes.
                uniqueTemplates.put("EMPTY-" + System.nanoTime(), t);
            }
        }

        List<Product> userDefaults = productRepository.findByUserAndIsDefault(user, true);
        Set<String> existingCodes = userDefaults.stream()
                .map(p -> p.getItemCode() != null ? p.getItemCode().toLowerCase().trim() : "")
                .collect(Collectors.toSet());

        List<Product> seeded = uniqueTemplates.values().stream()
                .filter(t -> {
                    String code = t.getItemCode() != null ? t.getItemCode().toLowerCase().trim() : "";
                    return code.isEmpty() || !existingCodes.contains(code);
                })
                .map(t -> {
                    Product p = new Product();
                    p.setUser(user);
                    p.setItemName(t.getItemName());
                    p.setItemCode(t.getItemCode());
                    p.setCategory(t.getCategory());
                    p.setSubcategory(t.getSubcategory());
                    p.setMrp(t.getMrp());
                    p.setSaleRate(t.getSaleRate());
                    p.setQuantity(0);
                    p.setExpiryDate(java.time.LocalDate.now().plusYears(1));
                    p.setDefault(true);
                    return p;
                }).collect(Collectors.toList());

        if (!seeded.isEmpty()) {
            productRepository.saveAll(seeded);
            auditLogService.log("SYSTEM", "User", user.getId(),
                    String.format("Initialized %d NEW default products for user %s", seeded.size(), user.getEmail()));
        }
    }
}
