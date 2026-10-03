package com.smms.backend.repository;

import com.smms.backend.model.LocalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocalUserRepository extends JpaRepository<LocalUser, Long> {

    List<LocalUser> findAllByOrderByCreatedAtAsc();

    Optional<LocalUser> findByEmailIgnoreCase(String email);
}
