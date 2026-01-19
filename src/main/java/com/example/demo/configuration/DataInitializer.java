package com.example.demo.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@Configuration
public class DataInitializer {
    
    @Bean
    CommandLineRunner initUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // ADMIN
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setFullName("Administrator");
                admin.setEmail("admin@mail.com");
                admin.setRole("ROLE_ADMIN");

                userRepository.save(admin);
            }

            // PM
            if (userRepository.findByUsername("pm").isEmpty()) {
                User pm = new User();
                pm.setUsername("pm");
                pm.setPassword(passwordEncoder.encode("123456"));
                pm.setFullName("Project Manager");
                pm.setEmail("pm@mail.com");
                pm.setRole("ROLE_PM");

                userRepository.save(pm);
            }

            
        };
    }
    
}
