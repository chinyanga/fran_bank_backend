package com.core.banking.service.impl;

import com.core.banking.dto.request.RegisterRequest;
import com.core.banking.dto.response.UserResponse;
import com.core.banking.entity.Role;
import com.core.banking.entity.User;
import com.core.banking.enums.UserStatus;
import com.core.banking.exception.EmailAlreadyExistsException;
import com.core.banking.repository.RoleRepository;
import com.core.banking.repository.UserRepository;
import com.core.banking.service.UserService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must not exceed 72 UTF-8 bytes."
            );
        }

        if (userRepository.existsByEmailNormalized(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException(
                        "CUSTOMER role has not been configured."
                ));

        User user = new User();

        // We will add a controlled creation method to User next.
        user.create(
                request.firstName().trim(),
                request.lastName().trim(),
                request.email().trim(),
                normalizedEmail,
                request.phoneNumber().trim(),
                passwordEncoder.encode(request.password()),
                UserStatus.ACTIVE
        );

        user.assignRole(customerRole);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return toResponse(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}