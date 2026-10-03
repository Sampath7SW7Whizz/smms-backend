package com.smms.backend.controller;

import com.smms.backend.model.StoreProfile;
import com.smms.backend.service.StoreProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class StoreProfileController {

    @Autowired
    private StoreProfileService storeProfileService;

    // Directory for storing uploaded images
    private final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public ResponseEntity<StoreProfile> getProfile() {
        return ResponseEntity.ok(storeProfileService.getStoreProfile());
    }

    @PutMapping
    public ResponseEntity<StoreProfile> updateProfile(@RequestBody StoreProfile profile) {
        return ResponseEntity.ok(storeProfileService.updateStoreProfile(profile));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }

        try {
            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // Save the file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the file URL (assuming static resource serving is configured or just
            // the filename for now)
            // In a real app, you'd configure a resource handler to serve files from
            // UPLOAD_DIR
            return ResponseEntity.ok("/uploads/" + fileName);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Could not upload file: " + e.getMessage());
        }
    }
}
