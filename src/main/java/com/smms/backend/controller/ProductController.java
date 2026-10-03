package com.smms.backend.controller;

import com.smms.backend.model.Product;
import com.smms.backend.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173") // Allow requests from frontend
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{itemCode}")
    public ResponseEntity<Product> getProductByItemCode(@PathVariable String itemCode) {
        return productService.getProductByItemCode(itemCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Product> searchProducts(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "8") int limit) {
        return productService.searchProducts(query, limit);
    }

    @PostMapping("/initialize-defaults")
    public ResponseEntity<Void> initializeDefaultProductsForCurrentUser() {
        productService.initializeDefaultsForCurrentUser();
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> createProducts(@RequestBody List<Product> products) {
        try {
            if (products == null || products.isEmpty()) {
                return ResponseEntity.badRequest().body("Product list is empty");
            }
            
            // Validate each product has required pricing fields
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);

                if (p.getMrp() == null || p.getMrp() <= 0) {
                    return ResponseEntity.badRequest().body("Row " + (i + 1) + ": MRP must be greater than 0");
                }
                if (p.getSaleRate() == null || p.getSaleRate() <= 0) {
                    return ResponseEntity.badRequest().body("Row " + (i + 1) + ": Sale Rate must be greater than 0");
                }
            }
            
            List<Product> savedProducts = productService.saveAllProducts(products);
            return ResponseEntity.ok(savedProducts);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Bulk upload error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid request format: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productService.getProductById(id)
                .map(product -> {
                    product.setItemName(productDetails.getItemName());
                    product.setItemCode(productDetails.getItemCode());
                    product.setCategory(productDetails.getCategory());
                    product.setSubcategory(productDetails.getSubcategory());
                    product.setMrp(productDetails.getMrp());
                    product.setSaleRate(productDetails.getSaleRate());
                    product.setQuantity(productDetails.getQuantity());
                    product.setExpiryDate(productDetails.getExpiryDate());
                    Product updatedProduct = productService.saveProduct(product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
