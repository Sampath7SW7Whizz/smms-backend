package com.smms.backend.service;

import com.smms.backend.dto.DashboardStats;
import com.smms.backend.model.Product;
import com.smms.backend.model.Sale;
import com.smms.backend.model.User;
import com.smms.backend.repository.ProductRepository;
import com.smms.backend.repository.SaleRepository;
import com.smms.backend.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private SaleRepository saleRepository;

        public DashboardStats getStats() {
                User user = AuthContext.getCurrentUser();

                List<Sale> sales = saleRepository.findByUser(user);
                Double totalRevenue = sales.stream()
                                .mapToDouble(Sale::getGrandTotal)
                                .sum();

                List<Product> products = productRepository.findByUser(user);
                Long totalProducts = (long) products.size();
                Long lowStockCount = products.stream()
                                .filter(p -> p.getQuantity() < 5 && p.getQuantity() > 0)
                                .count();
                Long outOfStockCount = products.stream()
                                .filter(p -> p.getQuantity() == 0)
                                .count();

                // New Metrics
                Double inventoryValue = products.stream()
                                .mapToDouble(p -> p.getQuantity() * p.getSaleRate())
                                .sum();

                java.time.LocalDate sevenDaysFromNow = java.time.LocalDate.now().plusDays(7);
                Long expiringSoonCount = products.stream()
                                .filter(p -> p.getExpiryDate() != null
                                                && !p.getExpiryDate().isBefore(java.time.LocalDate.now())
                                                && p.getExpiryDate().isBefore(sevenDaysFromNow))
                                .count();

                List<Sale> recentOrders = saleRepository.findTop5ByUserOrderByDateDesc(user);

                // Top Selling Logic (Simplified: aggregations in memory for now)
                java.util.Map<String, Integer> productSales = new java.util.HashMap<>();
                for (Sale sale : sales) {
                        for (com.smms.backend.model.SaleItem item : sale.getItems()) {
                                productSales.put(item.getItemName(),
                                                productSales.getOrDefault(item.getItemName(), 0) + item.getQuantity());
                        }
                }

                List<java.util.Map<String, Object>> topSellingProducts = productSales.entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(5)
                                .map(e -> {
                                        java.util.Map<String, Object> map = new java.util.HashMap<>();
                                        map.put("name", e.getKey());
                                        map.put("sold", e.getValue());
                                        return map;
                                })
                                .collect(java.util.stream.Collectors.toList());

                // Revenue Data (Last 6 months)
                List<java.util.Map<String, Object>> revenueData = new java.util.ArrayList<>();
                java.time.YearMonth currentMonth = java.time.YearMonth.now();
                for (int i = 5; i >= 0; i--) {
                        java.time.YearMonth targetMonth = currentMonth.minusMonths(i);
                        String monthName = targetMonth.getMonth().getDisplayName(java.time.format.TextStyle.SHORT,
                                        java.util.Locale.ENGLISH);

                        double monthlyRevenue = sales.stream()
                                        .filter(s -> java.time.YearMonth.from(s.getDate()).equals(targetMonth))
                                        .mapToDouble(Sale::getGrandTotal)
                                        .sum();

                        java.util.Map<String, Object> dataPoint = new java.util.HashMap<>();
                        dataPoint.put("month", monthName);
                        dataPoint.put("revenue", monthlyRevenue);
                        dataPoint.put("newUsers", 0); // Placeholder
                        revenueData.add(dataPoint);
                }

                return new DashboardStats(totalRevenue, totalProducts, lowStockCount, outOfStockCount,
                                inventoryValue, expiringSoonCount, recentOrders, topSellingProducts, revenueData);
        }
}
