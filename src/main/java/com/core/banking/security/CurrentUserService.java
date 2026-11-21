package com.core.banking.security;

import com.core.banking.entity.User;
import com.core.banking.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No authenticated customer found.");
        }

        String email = authentication.getName();

        return userRepository.findByEmailNormalized(
                email.trim().toLowerCase()
        ).orElseThrow(() ->
                new IllegalStateException("Authenticated customer not found."));
    }
}