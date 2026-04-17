package com.giglog.user.controller;

import com.giglog.common.dto.response.ApiResponse;
import com.giglog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("User Service is healthy on port 8081");
    }
}
