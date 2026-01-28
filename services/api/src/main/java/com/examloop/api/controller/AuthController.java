package com.examloop.api.controller;

import com.examloop.api.dto.LoginRequest;
import com.examloop.api.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/anon")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestHeader(value = "X-Device-Id", required = false) String headerDeviceId,
            @Valid @RequestBody(required = false) LoginRequest request) {
        
        // Accept device ID from either header or body
        String deviceId = headerDeviceId != null ? headerDeviceId : 
                         (request != null ? request.getDeviceId() : null);
        
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new LoginResponse(null, "Device ID is required in X-Device-Id header or request body")
            );
        }
        
        // For simplicity, we're returning the device ID as the token
        // In production, you'd generate a proper JWT token
        return ResponseEntity.ok(new LoginResponse(deviceId, "Login successful"));
    }
}
