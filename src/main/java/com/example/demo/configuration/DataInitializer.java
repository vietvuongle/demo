package com.example.demo.configuration;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.RoleCode;
import com.example.demo.enums.UserStatus;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // Tạo roles nếu chưa tồn tại
            createRoleIfNotExists(roleRepository, RoleCode.ADMIN, "Quản trị");
            createRoleIfNotExists(roleRepository, RoleCode.PM, "Quản lý dự án");
            createRoleIfNotExists(roleRepository, RoleCode.MEMBER, "Thành viên");
            createRoleIfNotExists(roleRepository, RoleCode.LEADER, "Trưởng nhóm");

            // Tạo user ADMIN nếu chưa tồn tại
            createUserIfNotExists(userRepository, roleRepository, "admin", "password", "Admin System", "admin@example.com", RoleCode.ADMIN);

            // Tạo user PM nếu chưa tồn tại
            createUserIfNotExists(userRepository, roleRepository, "pm1", "password", "Phan Minh (PM)", "pm1@example.com", RoleCode.PM);
        };
    }

    private void createRoleIfNotExists(RoleRepository roleRepository, RoleCode code, String name) {
        Optional<Role> existing = roleRepository.findByCode(code);
        if (existing.isEmpty()) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            roleRepository.save(role);
        }
    }

    private void createUserIfNotExists(UserRepository userRepository, RoleRepository roleRepository, String username, String plainPassword, String fullName, String email, RoleCode... roleCodes) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(plainPassword));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            for (RoleCode code : roleCodes) {
                roleRepository.findByCode(code).ifPresent(roles::add);
            }
            user.setRoles(roles);

            userRepository.save(user);
        }
    }
}
