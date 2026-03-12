package com.chakray.usersapi.controller;

import com.chakray.usersapi.model.User;
import com.chakray.usersapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) String sortedBy,
            @RequestParam(required = false) String filter) {

        List<User> result;

        if (filter != null && !filter.isBlank()) {
            result = userService.filterUsers(filter);
        } else {
            result = userService.getUsers(sortedBy);
        }

        List<User> sanitized = result.stream()
                .map(u -> new User(
                        u.getId(),
                        u.getEmail(),
                        u.getName(),
                        u.getPhone(),
                        null,
                        u.getTax_id(),
                        u.getCreated_at(),
                        u.getAddresses()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sanitized);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            User created = userService.createUser(user);
            User sanitized = new User(
                    created.getId(),
                    created.getEmail(),
                    created.getName(),
                    created.getPhone(),
                    null,
                    created.getTax_id(),
                    created.getCreated_at(),
                    created.getAddresses()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(sanitized);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> fields) {
        return userService.updateUser(id, fields)
                .map(u -> {
                    User sanitized = new User(
                            u.getId(),
                            u.getEmail(),
                            u.getName(),
                            u.getPhone(),
                            null,
                            u.getTax_id(),
                            u.getCreated_at(),
                            u.getAddresses()
                    );
                    return ResponseEntity.ok(sanitized);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}