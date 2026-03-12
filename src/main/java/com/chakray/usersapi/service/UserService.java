package com.chakray.usersapi.service;

import com.chakray.usersapi.model.User;
import com.chakray.usersapi.util.AesEncryptionUtil;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final ZoneId MADAGASCAR_ZONE = ZoneId.of("Indian/Antananarivo");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?\\d{1,3}[\\s-]?)?(\\d{10})$");
    private static final Pattern TAX_ID_PATTERN = Pattern.compile("^[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}$");

    public UserService() {
        users.add(new User(
                UUID.randomUUID(),
                "user1@mail.com",
                "user1",
                "+1 5555555555",
                AesEncryptionUtil.encrypt("password123"),
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
                AesEncryptionUtil.encrypt("password123"),
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
                AesEncryptionUtil.encrypt("password123"),
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
        String[] parts = filter.split("[+\\s]");
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
                    case "phone" -> {
                        String phone = (String) value;
                        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
                            throw new IllegalArgumentException("Phone must be 10 digits and may include country code");
                        }
                        u.setPhone(phone);
                    }
                    case "tax_id" -> {
                        String taxId = (String) value;
                        if (taxId == null || !TAX_ID_PATTERN.matcher(taxId).matches()) {
                            throw new IllegalArgumentException("tax_id must have RFC format");
                        }
                        boolean taxIdExists = users.stream()
                                .anyMatch(other -> !other.getId().equals(u.getId()) && other.getTax_id().equals(taxId));
                        if (taxIdExists) {
                            throw new IllegalArgumentException("tax_id already exists");
                        }
                        u.setTax_id(taxId);
                    }
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