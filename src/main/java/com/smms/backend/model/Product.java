package com.smms.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "products",
    uniqueConstraints = @UniqueConstraint(columnNames = { "item_code", "user_id" }),
    indexes = {
        @Index(name = "idx_products_user_item_name", columnList = "user_id,item_name")
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = true)
    private String itemName;

    @Column
    private String itemCode;

    @Column(nullable = true)
    private String category;

    private String subcategory;

    @Column(nullable = false)
    private Double mrp;

    @Column(nullable = false)
    private Double saleRate;

    @Column(nullable = true)
    private Integer quantity;

    @Column(nullable = true)
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate expiryDate;

    @Column(nullable = true)
    private Boolean isDefault = false;

    public Product() {
    }

    public Product(User user, String itemName, String itemCode, String category, String subcategory, Double mrp,
            Double saleRate,
            Integer quantity, LocalDate expiryDate) {
        this(user, itemName, itemCode, category, subcategory, mrp, saleRate, quantity,
                expiryDate, false);
    }

    public Product(User user, String itemName, String itemCode, String category, String subcategory, Double mrp,
            Double saleRate,
            Integer quantity, LocalDate expiryDate, Boolean isDefault) {
        this.user = user;
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.category = category;
        this.subcategory = subcategory;
        this.mrp = mrp;
        this.saleRate = saleRate;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault != null ? isDefault : false;
    }

    // Backward-compatible constructors for legacy seed loaders.
    public Product(User user, String itemName, String itemCode, String category, String subcategory, Double mrp,
            Double saleRate, Integer quantity, String ignoredTaxRate, String ignoredSupplierName, LocalDate expiryDate) {
        this(user, itemName, itemCode, category, subcategory, mrp, saleRate, quantity, expiryDate, false);
    }

    public Product(User user, String itemName, String itemCode, String category, String subcategory, Double mrp,
            Double saleRate, Integer quantity, String ignoredTaxRate, String ignoredSupplierName, LocalDate expiryDate,
            Boolean isDefault) {
        this(user, itemName, itemCode, category, subcategory, mrp, saleRate, quantity, expiryDate, isDefault);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public Double getMrp() {
        return mrp;
    }

    public void setMrp(Double mrp) {
        this.mrp = mrp;
    }

    public Double getSaleRate() {
        return saleRate;
    }

    public void setSaleRate(Double saleRate) {
        this.saleRate = saleRate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @JsonProperty("default")
    public Boolean getIsDefault() {
        return isDefault != null ? isDefault : false;
    }

    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault != null ? isDefault : false;
    }
}
