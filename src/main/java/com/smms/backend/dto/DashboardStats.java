package com.smms.backend.dto;

public class DashboardStats {
    private Double totalRevenue;
    private Long totalProducts;
    private Long lowStockCount;
    private Long outOfStockCount;
    private Double inventoryValue;
    private Long expiringSoonCount;
    private java.util.List<com.smms.backend.model.Sale> recentOrders;
    private java.util.List<java.util.Map<String, Object>> topSellingProducts;
    private java.util.List<java.util.Map<String, Object>> revenueData;

    public DashboardStats(Double totalRevenue, Long totalProducts, Long lowStockCount, Long outOfStockCount,
            Double inventoryValue, Long expiringSoonCount,
            java.util.List<com.smms.backend.model.Sale> recentOrders,
            java.util.List<java.util.Map<String, Object>> topSellingProducts,
            java.util.List<java.util.Map<String, Object>> revenueData) {
        this.totalRevenue = totalRevenue;
        this.totalProducts = totalProducts;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
        this.inventoryValue = inventoryValue;
        this.expiringSoonCount = expiringSoonCount;
        this.recentOrders = recentOrders;
        this.topSellingProducts = topSellingProducts;
        this.revenueData = revenueData;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(Long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public Long getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(Long outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public Double getInventoryValue() {
        return inventoryValue;
    }

    public void setInventoryValue(Double inventoryValue) {
        this.inventoryValue = inventoryValue;
    }

    public Long getExpiringSoonCount() {
        return expiringSoonCount;
    }

    public void setExpiringSoonCount(Long expiringSoonCount) {
        this.expiringSoonCount = expiringSoonCount;
    }

    public java.util.List<com.smms.backend.model.Sale> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(java.util.List<com.smms.backend.model.Sale> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public java.util.List<java.util.Map<String, Object>> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(java.util.List<java.util.Map<String, Object>> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public java.util.List<java.util.Map<String, Object>> getRevenueData() {
        return revenueData;
    }

    public void setRevenueData(java.util.List<java.util.Map<String, Object>> revenueData) {
        this.revenueData = revenueData;
    }
}
