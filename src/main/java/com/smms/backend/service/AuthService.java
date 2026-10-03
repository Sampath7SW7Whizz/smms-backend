package com.smms.backend.service;

import com.smms.backend.dto.LoginRequest;
import com.smms.backend.dto.RegisterRequest;
import com.smms.backend.model.StoreProfile;
import com.smms.backend.model.User;
import com.smms.backend.repository.StoreProfileRepository;
import com.smms.backend.repository.UserRepository;
import com.smms.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreProfileRepository storeProfileRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    private String hashPassword(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (req.getPassword() == null || req.getPassword().isBlank())
            throw new IllegalArgumentException("Password is required");
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase()))
            throw new IllegalArgumentException("Email already registered");

        User user = new User();
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail().trim().toLowerCase());
        user.setPhone(req.getPhone());
        user.setPasswordHash(hashPassword(req.getPassword()));
        user.setShopName(req.getShopName());
        user.setShopAddress(req.getShopAddress());
        user.setReferredBy(req.getReferredBy());
        user.setRole("USER");
        User saved = userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole());

        StoreProfile profile = new StoreProfile();
        profile.setUser(saved);
        profile.setStoreName(req.getShopName());
        profile.setOwnerName(req.getFullName());
        profile.setEmail(req.getEmail().trim().toLowerCase());
        profile.setPhone(req.getPhone());
        profile.setAddress(req.getShopAddress());
        storeProfileRepository.save(profile);

        productService.initializeDefaultProducts(saved);

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", saved.getId());
        userMap.put("fullName", saved.getFullName());
        userMap.put("email", saved.getEmail());
        userMap.put("phone", Optional.ofNullable(saved.getPhone()).orElse(""));
        userMap.put("shopName", Optional.ofNullable(saved.getShopName()).orElse(""));
        userMap.put("role", saved.getRole());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", jwtToken);
        result.put("user", userMap);
        result.put("message", "Registration successful");
        return result;
    }

    public Map<String, Object> login(LoginRequest req) {
        String identifier = req.resolveIdentifier();

        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Email or phone is required");
        }

        User user = userRepository.findByEmail(identifier.toLowerCase())
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email/phone or password"));

        if (!user.getPasswordHash().equals(hashPassword(req.getPassword()))) {
            throw new IllegalArgumentException("Invalid email/phone or password");
        }

        String jwtToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("fullName", user.getFullName());
        userMap.put("email", user.getEmail());
        userMap.put("phone", Optional.ofNullable(user.getPhone()).orElse(""));
        userMap.put("shopName", Optional.ofNullable(user.getShopName()).orElse(""));
        userMap.put("role", user.getRole());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", jwtToken);
        result.put("user", userMap);
        result.put("message", "Login successful");
        return result;
    }
}
