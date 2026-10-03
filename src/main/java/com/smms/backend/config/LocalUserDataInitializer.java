package com.smms.backend.config;

import com.smms.backend.model.LocalUser;
import com.smms.backend.repository.LocalUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalUserDataInitializer {

    @Bean
    CommandLineRunner seedLocalUsers(LocalUserRepository localUserRepository) {
        return args -> {
            if (localUserRepository.count() > 0) {
                return;
            }

            localUserRepository.save(new LocalUser("Ananya Retail", "ananya@smms.local"));
            localUserRepository.save(new LocalUser("Vikram Stores", "vikram@smms.local"));
        };
    }
}
