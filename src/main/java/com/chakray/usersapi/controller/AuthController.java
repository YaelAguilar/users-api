package com.chakray.usersapi.controller;

import com.chakray.usersapi.model.User;
import com.chakray.usersapi.service.UserService;
import com.chakray.usersapi.util.AesEncryptionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String taxId = credentials.get("tax_id");
        String password = credentials.get("password");

        if (taxId == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "tax_id and password are required"));
        }

        Optional<User> userOpt = userService.findByTaxId(taxId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        String decryptedPassword = AesEncryptionUtil.decrypt(user.getPassword());

        if (!decryptedPassword.equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "tax_id", user.getTax_id(),
                "name", user.getName()
        ));
    }
}