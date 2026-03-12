package com.chakray.usersapi.service;

import com.chakray.usersapi.model.User;
import com.chakray.usersapi.util.AesEncryptionUtil;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final ZoneId MADAGASCAR_ZONE = ZoneId.of("Indian/Antananarivo");

    public UserService() {
        users.add(new User(
                UUID.randomUUID(),
                "user1@mail.com",
                "user1",
                "+1 5555555555",
                AesEncryptionUtil.encrypt("7c4a8d09ca3762af61e59520943dc26494f8941b"),
                "AARR990101XXX",
                "01-01-2026 00:00",
                List.of(
                        new com.chakray.usersapi.model.Address(1, "workaddress", "street No. 1", "UK"),
                        new com.chakray.usersapi.model.Address(2, "homeaddress", "street No. 2", "AU")
                )
        ));
        users.add(new User(
                UUID.randomUUID(),
                "user2@mail.com",
                "user2",
                "+1 5555555556",
                AesEncryptionUtil.encrypt("7c4a8d09ca3762af61e59520943dc26494f8941b"),
                "BBRR990101YYY",
                "01-01-2026 00:00",
                List.of(
                        new com.chakray.usersapi.model.Address(1, "workaddress", "street No. 3", "US"),
                        new com.chakray.usersapi.model.Address(2, "homeaddress", "street No. 4", "MX")
                )
        ));
        users.add(new User(
                UUID.randomUUID(),
                "user3@mail.com",
                "user3",
                "+1 5555555557",
                AesEncryptionUtil.encrypt("7c4a8d09ca3762af61e59520943dc26494f8941b"),
                "CCRR990101ZZZ",
                "01-01-2026 00:00",
                List.of(
                        new com.chakray.usersapi.model.Address(1, "workaddress", "street No. 5", "CA"),
                        new com.chakray.usersapi.model.Address(2, "homeaddress", "street No. 6", "FR")
                )
        ));
    }

    private String getCurrentMadagascarTime() {
        return ZonedDateTime.now(MADAGASCAR_ZONE).format(FORMATTER);
    }

    private String getFieldValue(User u, String field) {
        return switch (field) {
            case "email" -> u.getEmail();
            case "name" -> u.getName();
            case "phone" -> u.getPhone();
            case "tax_id" -> u.getTax_id();
            case "created_at" -> u.getCreated_at();
            default -> u.getId().toString();
        };
    }

    public List<User> getUsers(String sortedBy) {
        List<User> result = new ArrayList<>(users);
        if (sortedBy != null && !sortedBy.isBlank()) {
            result.sort(Comparator.comparing(u -> getFieldValue(u, sortedBy)));
        }
        return result;
    }

    public List<User> filterUsers(String filter) {
        String[] parts = filter.split("\\+");
        if (parts.length != 3) return Collections.emptyList();

        String field = parts[0];
        String operator = parts[1];
        String value = parts[2];

        return users.stream().filter(u -> {
            String fieldValue = getFieldValue(u, field);
            return switch (operator) {
                case "co" -> fieldValue.contains(value);
                case "eq" -> fieldValue.equals(value);
                case "sw" -> fieldValue.startsWith(value);
                case "ew" -> fieldValue.endsWith(value);
                default -> false;
            };
        }).toList();
    }

    public User createUser(User user) {
        boolean taxIdExists = users.stream()
                .anyMatch(u -> u.getTax_id().equals(user.getTax_id()));
        if (taxIdExists) {
            throw new IllegalArgumentException("tax_id already exists");
        }
        user.setId(UUID.randomUUID());
        user.setCreated_at(getCurrentMadagascarTime());
        user.setPassword(AesEncryptionUtil.encrypt(user.getPassword()));
        users.add(user);
        return user;
    }

    public Optional<User> updateUser(UUID id, Map<String, Object> fields) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().map(u -> {
            fields.forEach((key, value) -> {
                switch (key) {
                    case "email" -> u.setEmail((String) value);
                    case "name" -> u.setName((String) value);
                    case "phone" -> u.setPhone((String) value);
                    case "tax_id" -> u.setTax_id((String) value);
                    case "password" -> u.setPassword(AesEncryptionUtil.encrypt((String) value));
                }
            });
            return u;
        });
    }

    public boolean deleteUser(UUID id) {
        return users.removeIf(u -> u.getId().equals(id));
    }

    public Optional<User> findByTaxId(String taxId) {
        return users.stream().filter(u -> u.getTax_id().equals(taxId)).findFirst();
    }
}