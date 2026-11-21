package com.core.banking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoPasswordHashGenerator implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;

    public DemoPasswordHashGenerator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        System.out.println(
                "FRANBANK_DEMO_PASSWORD_HASH="
                        + passwordEncoder.encode("FranBank@Demo2026!")
        );
    }
}