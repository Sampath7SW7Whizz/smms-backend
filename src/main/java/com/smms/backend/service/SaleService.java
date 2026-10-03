package com.smms.backend.service;

import com.smms.backend.model.Sale;
import com.smms.backend.model.SaleItem;
import com.smms.backend.model.Product;
import com.smms.backend.model.User;
import com.smms.backend.repository.SaleRepository;
import com.smms.backend.repository.ProductRepository;
import com.smms.backend.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @CacheEvict(value = { "sales", "dailySales" }, allEntries = true)
    public Sale createSale(Sale sale) {
        User user = AuthContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User not authenticated. Please login again.");
        }

        // Idempotency check: if this exact request (by its client-generated
        // id) was already saved — typically a mobile offline-queue retry
        // whose earlier success response never reached the device — return
        // the existing sale rather than creating a duplicate and deducting
        // stock twice.
        if (sale.getClientRequestId() != null && !sale.getClientRequestId().isBlank()) {
            var existing = saleRepository.findByUserAndClientRequestId(user, sale.getClientRequestId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        sale.setUser(user);

        // Always use server's current time for accurate timestamps
        sale.setDate(LocalDateTime.now());

        // Generate UNIQUE sequential transaction ID (thread-safe with SERIALIZABLE
        // isolation)
        String transactionId = generateSequentialTransactionId(user);
        if (transactionId == null || transactionId.isEmpty()) {
            throw new RuntimeException("Failed to generate transaction ID. Please try again.");
        }
        sale.setTransactionId(transactionId);

        // Batch load all products first to avoid N+1 queries
        List<String> itemCodes = sale.getItems().stream()
                .map(SaleItem::getItemCode)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // Fetch all products in ONE query instead of multiple
        Map<String, Product> productMap = productRepository.findByUserAndItemCodeIn(user, itemCodes)
                .stream()
                .collect(Collectors.toMap(
                        p -> p.getItemCode().toLowerCase(),
                        p -> p,
                        (p1, p2) -> p1));

        // Ensure bidirectional relationship is set and deduct stock
        if (sale.getItems() != null) {
            for (SaleItem item : sale.getItems()) {
                item.setSale(sale);

                // Deduct stock from product inventory using pre-loaded map
                if (item.getItemCode() != null) {
                    Product product = productMap.get(item.getItemCode().toLowerCase());
                    if (product != null) {
                        int currentQty = product.getQuantity() != null ? product.getQuantity() : 0;
                        int itemQty = item.getQuantity() != null ? item.getQuantity() : 0;
                        int newQuantity = currentQty - itemQty;
                        product.setQuantity(Math.max(0, newQuantity));
                    }
                }
            }

            // Batch save all products in ONE query
            if (!productMap.isEmpty()) {
                productRepository.saveAll(productMap.values());
            }
        }

        Sale savedSale = saleRepository.save(sale);

        // Log the sale
        if (user != null) {
            int itemCount = sale.getItems() != null ? sale.getItems().size() : 0;
            auditLogService.log("SALE_CREATED", "Sale", savedSale.getId(),
                    String.format("Sale completed with %d items for ₹%.2f", itemCount, savedSale.getGrandTotal()));
        }

        return savedSale;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "sales", key = "'all_' + (T(com.smms.backend.security.AuthContext).getCurrentUser() != null ? T(com.smms.backend.security.AuthContext).getCurrentUser().id : 'anonymous')")
    public List<Sale> getAllSales() {
        User user = AuthContext.getCurrentUser();
        if (user == null) {
            return java.util.Collections.emptyList();
        }
        return saleRepository.findByUserWithItemsOrderByDateDesc(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "dailySales", key = "T(java.time.LocalDate).now().toString() + '_' + (T(com.smms.backend.security.AuthContext).getCurrentUser() != null ? T(com.smms.backend.security.AuthContext).getCurrentUser().id : 'anonymous')")
    public Double getTodaySales() {
        User user = AuthContext.getCurrentUser();
        if (user == null) {
            return 0.0;
        }
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Sale> todaysSales = saleRepository.findByUserAndDateBetween(user, start, end);
        return todaysSales.stream()
                .mapToDouble(Sale::getGrandTotal)
                .sum();
    }

    /**
     * Generates a sequential transaction ID in format INV000001, INV000002, etc.
     * Thread-safe with SERIALIZABLE transaction isolation.
     */
    private String generateSequentialTransactionId(User user) {
        // Get the latest transaction ID for this user from database
        String lastTransactionId = saleRepository.findTopByUserOrderByIdDesc(user)
                .map(Sale::getTransactionId)
                .orElse("INV000000");

        // Extract the numeric part
        int nextNumber = 1;
        if (lastTransactionId != null && lastTransactionId.startsWith("INV")) {
            try {
                String numericPart = lastTransactionId.substring(3); // Remove "INV" prefix
                nextNumber = Integer.parseInt(numericPart) + 1;
            } catch (NumberFormatException e) {
                // If parsing fails, start from 1
                nextNumber = 1;
            }
        }

        // Format as INV000001, INV000002, etc. (6 digits with leading zeros)
        return String.format("INV%06d", nextNumber);
    }

    /**
     * Gets the next sequential transaction ID (for display purposes before saving).
     */
    public String getNextTransactionId() {
        User user = AuthContext.getCurrentUser();
        if (user == null) {
            return "INV000001";
        }

        // Get the latest transaction ID for preview
        String lastTransactionId = saleRepository.findTopByUserOrderByIdDesc(user)
                .map(Sale::getTransactionId)
                .orElse("INV000000");

        int nextNumber = 1;
        if (lastTransactionId != null && lastTransactionId.startsWith("INV")) {
            try {
                String numericPart = lastTransactionId.substring(3);
                nextNumber = Integer.parseInt(numericPart) + 1;
            } catch (NumberFormatException e) {
                nextNumber = 1;
            }
        }

        return String.format("INV%06d", nextNumber);
    }
}
