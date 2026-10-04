package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.rest.dto.LoginRequest;
import com.gokul.trustdesk.application.rest.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Value("${app.security.api-token:demo-secret-token-123}")
    private String apiToken;

    @Value("${app.security.admin-username:agent}")
    private String expectedUsername;

    @Value("${app.security.admin-password:trustdesk123}")
    private String expectedPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (expectedUsername.equals(request.username()) && expectedPassword.equals(request.password())) {
            return ResponseEntity.ok(new LoginResponse(apiToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid credentials"));
    }
}