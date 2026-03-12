package com.chakray.usersapi;

import com.chakray.usersapi.model.User;
import com.chakray.usersapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UsersApiApplicationTests {

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService();
	}

	@Test
	void shouldReturnThreeUsersOnInit() {
		List<User> users = userService.getUsers(null);
		assertEquals(3, users.size());
	}

	@Test
	void shouldReturnUsersSortedByName() {
		List<User> users = userService.getUsers("name");
		assertEquals("user1", users.get(0).getName());
		assertEquals("user2", users.get(1).getName());
		assertEquals("user3", users.get(2).getName());
	}

	@Test
	void shouldFilterUsersByNameContains() {
		List<User> users = userService.filterUsers("name+co+user");
		assertEquals(3, users.size());
	}

	@Test
	void shouldFilterUsersByTaxIdEquals() {
		List<User> users = userService.filterUsers("tax_id+eq+AARR990101XXX");
		assertEquals(1, users.size());
		assertEquals("AARR990101XXX", users.getFirst().getTax_id());
	}

	@Test
	void shouldFilterUsersByEmailEndsWith() {
		List<User> users = userService.filterUsers("email+ew+mail.com");
		assertEquals(3, users.size());
	}

	@Test
	void shouldCreateUserSuccessfully() {
		User newUser = new User();
		newUser.setEmail("user4@mail.com");
		newUser.setName("user4");
		newUser.setPhone("+1 5555555558");
		newUser.setPassword("password123");
		newUser.setTax_id("DDRR990101WWW");
		newUser.setAddresses(List.of());

		User created = userService.createUser(newUser);

		assertNotNull(created.getId());
		assertNotNull(created.getCreated_at());
		assertEquals(4, userService.getUsers(null).size());
	}

	@Test
	void shouldThrowExceptionWhenTaxIdIsDuplicated() {
		User newUser = new User();
		newUser.setEmail("duplicate@mail.com");
		newUser.setName("duplicate");
		newUser.setPhone("+1 5555555559");
		newUser.setPassword("password123");
		newUser.setTax_id("AARR990101XXX");
		newUser.setAddresses(List.of());

		assertThrows(IllegalArgumentException.class, () -> userService.createUser(newUser));
	}

	@Test
	void shouldUpdateUserName() {
		UUID id = userService.getUsers(null).getFirst().getId();
		Optional<User> updated = userService.updateUser(id, Map.of("name", "updatedName"));
		assertTrue(updated.isPresent());
		assertEquals("updatedName", updated.get().getName());
	}

	@Test
	void shouldDeleteUserById() {
		UUID id = userService.getUsers(null).getFirst().getId();
		boolean deleted = userService.deleteUser(id);
		assertTrue(deleted);
		assertEquals(2, userService.getUsers(null).size());
	}

	@Test
	void shouldReturnUserByTaxId() {
		Optional<User> user = userService.findByTaxId("AARR990101XXX");
		assertTrue(user.isPresent());
		assertEquals("user1", user.get().getName());
	}

	@Test
	void shouldReturnEmptyWhenTaxIdNotFound() {
		Optional<User> user = userService.findByTaxId("XXXX000000XXX");
		assertFalse(user.isPresent());
	}
}