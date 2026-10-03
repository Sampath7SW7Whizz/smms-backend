package com.smms.backend.service;

import com.smms.backend.model.StoreProfile;
import com.smms.backend.model.User;
import com.smms.backend.repository.StoreProfileRepository;
import com.smms.backend.security.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreProfileService {

    @Autowired
    private StoreProfileRepository storeProfileRepository;

    public StoreProfile getStoreProfile() {
        User user = AuthContext.getCurrentUser();
        return storeProfileRepository.findByUser(user).map(profile -> {
            boolean updated = false;
            if (profile.getAddress() == null || profile.getAddress().isEmpty()) {
                profile.setAddress("123, Market Road, Madanapalle, Andhra Pradesh - 517325");
                updated = true;
            }
            if (profile.getPhone() == null || profile.getPhone().isEmpty()) {
                profile.setPhone("+91 9491301258");
                updated = true;
            }
            if (profile.getEmail() == null || profile.getEmail().isEmpty()) {
                profile.setEmail(user.getEmail());
                updated = true;
            }
            if (profile.getGstin() == null || profile.getGstin().isEmpty()) {
                profile.setGstin("37CPGPN5115Q1Z8");
                updated = true;
            }

            if (updated) {
                return storeProfileRepository.save(profile);
            }
            return profile;
        }).orElseGet(() -> {
            // Create a default profile for the user if none exists
            StoreProfile defaultProfile = new StoreProfile();
            defaultProfile.setUser(user);
            defaultProfile.setStoreName(user.getShopName() != null ? user.getShopName() : "My Superstore");
            defaultProfile.setAddress(user.getShopAddress() != null ? user.getShopAddress()
                    : "123, Market Road, Madanapalle, Andhra Pradesh - 517325");
            defaultProfile.setPhone(user.getPhone() != null ? user.getPhone() : "+91 9491301258");
            defaultProfile.setEmail(user.getEmail());
            defaultProfile.setOwnerName(user.getFullName());
            defaultProfile.setGstin("37CPGPN5115Q1Z8");
            // Set default business hours
            defaultProfile.setOpeningTime("7:00 AM");
            defaultProfile.setClosingTime("10:00 PM");
            defaultProfile.setWorkingDays("Monday to Sunday");
            return storeProfileRepository.save(defaultProfile);
        });
    }

    public StoreProfile updateStoreProfile(StoreProfile profile) {
        User user = AuthContext.getCurrentUser();
        StoreProfile existing = getStoreProfile();

        // Merge only non-null fields from incoming profile
        if (profile.getStoreName() != null) existing.setStoreName(profile.getStoreName());
        if (profile.getStoreId() != null) existing.setStoreId(profile.getStoreId());
        if (profile.getOwnerName() != null) existing.setOwnerName(profile.getOwnerName());
        if (profile.getEmail() != null) existing.setEmail(profile.getEmail());
        if (profile.getPhone() != null) existing.setPhone(profile.getPhone());
        if (profile.getAddress() != null) existing.setAddress(profile.getAddress());
        if (profile.getStoreType() != null) existing.setStoreType(profile.getStoreType());
        if (profile.getDescription() != null) existing.setDescription(profile.getDescription());
        if (profile.getWebsite() != null) existing.setWebsite(profile.getWebsite());
        if (profile.getLogoUrl() != null) existing.setLogoUrl(profile.getLogoUrl());
        if (profile.getQrCodeUrl() != null) existing.setQrCodeUrl(profile.getQrCodeUrl());
        if (profile.getGstin() != null) existing.setGstin(profile.getGstin());
        if (profile.getPan() != null) existing.setPan(profile.getPan());
        if (profile.getCurrency() != null) existing.setCurrency(profile.getCurrency());
        if (profile.getTaxRate() != null) existing.setTaxRate(profile.getTaxRate());
        if (profile.getEnableTax() != null) existing.setEnableTax(profile.getEnableTax());
        if (profile.getHomeDelivery() != null) existing.setHomeDelivery(profile.getHomeDelivery());
        if (profile.getParkingAvailable() != null) existing.setParkingAvailable(profile.getParkingAvailable());
        if (profile.getOnlineOrders() != null) existing.setOnlineOrders(profile.getOnlineOrders());
        if (profile.getParkingCapacity() != null) existing.setParkingCapacity(profile.getParkingCapacity());
        if (profile.getFacebookUrl() != null) existing.setFacebookUrl(profile.getFacebookUrl());
        if (profile.getInstagramUrl() != null) existing.setInstagramUrl(profile.getInstagramUrl());
        if (profile.getTwitterUrl() != null) existing.setTwitterUrl(profile.getTwitterUrl());
        if (profile.getLinkedinUrl() != null) existing.setLinkedinUrl(profile.getLinkedinUrl());
        if (profile.getBankName() != null) existing.setBankName(profile.getBankName());
        if (profile.getAccountHolder() != null) existing.setAccountHolder(profile.getAccountHolder());
        if (profile.getAccountNumber() != null) existing.setAccountNumber(profile.getAccountNumber());
        if (profile.getIfscCode() != null) existing.setIfscCode(profile.getIfscCode());
        if (profile.getUpiId() != null) existing.setUpiId(profile.getUpiId());
        if (profile.getRegNo() != null) existing.setRegNo(profile.getRegNo());
        if (profile.getCin() != null) existing.setCin(profile.getCin());
        // Business Hours
        if (profile.getOpeningTime() != null) existing.setOpeningTime(profile.getOpeningTime());
        if (profile.getClosingTime() != null) existing.setClosingTime(profile.getClosingTime());
        if (profile.getWorkingDays() != null) existing.setWorkingDays(profile.getWorkingDays());
        // Payment gateways
        if (profile.getPaytmMid() != null) existing.setPaytmMid(profile.getPaytmMid());
        if (profile.getPaytmKey() != null) existing.setPaytmKey(profile.getPaytmKey());
        if (profile.getPaytmEnabled() != null) existing.setPaytmEnabled(profile.getPaytmEnabled());
        if (profile.getPhonePeMerchantId() != null) existing.setPhonePeMerchantId(profile.getPhonePeMerchantId());
        if (profile.getPhonePeSaltKey() != null) existing.setPhonePeSaltKey(profile.getPhonePeSaltKey());
        if (profile.getPhonePeSaltIndex() != null) existing.setPhonePeSaltIndex(profile.getPhonePeSaltIndex());
        if (profile.getPhonePeEnabled() != null) existing.setPhonePeEnabled(profile.getPhonePeEnabled());
        if (profile.getRazorpayKeyId() != null) existing.setRazorpayKeyId(profile.getRazorpayKeyId());
        if (profile.getRazorpayKeySecret() != null) existing.setRazorpayKeySecret(profile.getRazorpayKeySecret());
        if (profile.getRazorpayEnabled() != null) existing.setRazorpayEnabled(profile.getRazorpayEnabled());
        if (profile.getPayuMerchantKey() != null) existing.setPayuMerchantKey(profile.getPayuMerchantKey());
        if (profile.getPayuSalt() != null) existing.setPayuSalt(profile.getPayuSalt());
        if (profile.getPayuEnabled() != null) existing.setPayuEnabled(profile.getPayuEnabled());

        return storeProfileRepository.save(existing);
    }
}
