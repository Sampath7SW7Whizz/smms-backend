package com.smms.backend.controller;

import com.smms.backend.dto.LocalUserRequest;
import com.smms.backend.model.LocalUser;
import com.smms.backend.repository.LocalUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class LocalUserController {

    private final LocalUserRepository localUserRepository;

    public LocalUserController(LocalUserRepository localUserRepository) {
        this.localUserRepository = localUserRepository;
    }

    @GetMapping
    public List<LocalUser> getUsers() {
        return localUserRepository.findAllByOrderByCreatedAtAsc();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody LocalUserRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();

        if (name.isEmpty() || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name and email are required"));
        }

        if (localUserRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A local user with this email already exists"));
        }

        LocalUser savedUser = localUserRepository.save(new LocalUser(name, email));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
