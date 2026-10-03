package com.smms.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class StoreProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // Store Info
    private String storeName;
    private String storeId; // accountId in frontend
    private String ownerName;
    private String email;
    private String phone;
    private String address;
    private String storeType; // Category
    private String description;
    private String website;

    // Branding
    @Column(columnDefinition = "TEXT")
    private String logoUrl;
    @Column(columnDefinition = "TEXT")
    private String qrCodeUrl;

    // Business & Tax
    private String gstin;
    private String pan;
    private String currency; // e.g. "INR"
    private Double taxRate;
    private Boolean enableTax;

    // Attributes / Features
    private Boolean homeDelivery;
    private Boolean parkingAvailable;
    private Boolean onlineOrders;
    private Integer parkingCapacity;

    // Social Media (Flattened)
    private String facebookUrl;
    private String instagramUrl;
    private String twitterUrl;
    private String linkedinUrl;

    // Payment Gateways (Simplified)
    // Paytm
    private String paytmMid;
    private String paytmKey;
    private Boolean paytmEnabled;

    // Banking Info
    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String ifscCode;
    private String upiId;

    // Business Registration
    private String regNo;
    private String cin;

    // PhonePe
    private String phonePeMerchantId;
    private String phonePeSaltKey;
    private Integer phonePeSaltIndex;
    private Boolean phonePeEnabled;

    // Razorpay
    private String razorpayKeyId;
    private String razorpayKeySecret;
    private Boolean razorpayEnabled;

    // PayU
    private String payuMerchantKey;
    private String payuSalt;
    private Boolean payuEnabled;

    // Business Hours
    private String openingTime;
    private String closingTime;
    private String workingDays;

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

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStoreType() {
        return storeType;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

    public Boolean getEnableTax() {
        return enableTax;
    }

    public void setEnableTax(Boolean enableTax) {
        this.enableTax = enableTax;
    }

    public Boolean getHomeDelivery() {
        return homeDelivery;
    }

    public void setHomeDelivery(Boolean homeDelivery) {
        this.homeDelivery = homeDelivery;
    }

    public Boolean getParkingAvailable() {
        return parkingAvailable;
    }

    public void setParkingAvailable(Boolean parkingAvailable) {
        this.parkingAvailable = parkingAvailable;
    }

    public Boolean getOnlineOrders() {
        return onlineOrders;
    }

    public void setOnlineOrders(Boolean onlineOrders) {
        this.onlineOrders = onlineOrders;
    }

    public Integer getParkingCapacity() {
        return parkingCapacity;
    }

    public void setParkingCapacity(Integer parkingCapacity) {
        this.parkingCapacity = parkingCapacity;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public void setInstagramUrl(String instagramUrl) {
        this.instagramUrl = instagramUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getPaytmMid() {
        return paytmMid;
    }

    public void setPaytmMid(String paytmMid) {
        this.paytmMid = paytmMid;
    }

    public String getPaytmKey() {
        return paytmKey;
    }

    public void setPaytmKey(String paytmKey) {
        this.paytmKey = paytmKey;
    }

    public Boolean getPaytmEnabled() {
        return paytmEnabled;
    }

    public void setPaytmEnabled(Boolean paytmEnabled) {
        this.paytmEnabled = paytmEnabled;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getPhonePeMerchantId() {
        return phonePeMerchantId;
    }

    public void setPhonePeMerchantId(String phonePeMerchantId) {
        this.phonePeMerchantId = phonePeMerchantId;
    }

    public String getPhonePeSaltKey() {
        return phonePeSaltKey;
    }

    public void setPhonePeSaltKey(String phonePeSaltKey) {
        this.phonePeSaltKey = phonePeSaltKey;
    }

    public Integer getPhonePeSaltIndex() {
        return phonePeSaltIndex;
    }

    public void setPhonePeSaltIndex(Integer phonePeSaltIndex) {
        this.phonePeSaltIndex = phonePeSaltIndex;
    }

    public Boolean getPhonePeEnabled() {
        return phonePeEnabled;
    }

    public void setPhonePeEnabled(Boolean phonePeEnabled) {
        this.phonePeEnabled = phonePeEnabled;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId;
    }

    public String getRazorpayKeySecret() {
        return razorpayKeySecret;
    }

    public void setRazorpayKeySecret(String razorpayKeySecret) {
        this.razorpayKeySecret = razorpayKeySecret;
    }

    public Boolean getRazorpayEnabled() {
        return razorpayEnabled;
    }

    public void setRazorpayEnabled(Boolean razorpayEnabled) {
        this.razorpayEnabled = razorpayEnabled;
    }

    public String getPayuMerchantKey() {
        return payuMerchantKey;
    }

    public void setPayuMerchantKey(String payuMerchantKey) {
        this.payuMerchantKey = payuMerchantKey;
    }

    public String getPayuSalt() {
        return payuSalt;
    }

    public void setPayuSalt(String payuSalt) {
        this.payuSalt = payuSalt;
    }

    public Boolean getPayuEnabled() {
        return payuEnabled;
    }

    public void setPayuEnabled(Boolean payuEnabled) {
        this.payuEnabled = payuEnabled;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }

    public String getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(String workingDays) {
        this.workingDays = workingDays;
    }
}
