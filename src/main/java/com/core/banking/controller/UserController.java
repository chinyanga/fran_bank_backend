package com.core.banking.controller;

import com.core.banking.dto.response.APIResponse;
import com.core.banking.dto.response.UserResponse;
import com.core.banking.entity.User;
import com.core.banking.security.CurrentUserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<APIResponse<UserResponse>> getMyProfile() {

        User user = currentUserService.getCurrentUser();

        UserResponse response = new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus().name(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(
                APIResponse.success(
                        200,
                        "Customer profile retrieved successfully",
                        response
                )
        );
    }
}