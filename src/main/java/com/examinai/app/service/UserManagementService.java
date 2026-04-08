package com.examinai.app.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.examinai.app.domain.user.Role;
import com.examinai.app.domain.user.RoleRepository;
import com.examinai.app.domain.user.User;
import com.examinai.app.domain.user.UserRepository;

@Service
public class UserManagementService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserManagementService(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<User> listUsers() {
		return userRepository.findAllWithRolesOrderedByEmail();
	}

	@Transactional(readOnly = true)
	public User getUserById(UUID id) {
		return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found."));
	}

	@Transactional(readOnly = true)
	public List<Role> allRolesOrdered() {
		return roleRepository.findAll(Sort.by("name"));
	}

	@Transactional(readOnly = true)
	public List<User> listInternsOrderedByEmail() {
		return userRepository.findAllWithRoleName("intern");
	}

	@Transactional
	public User createUser(String email, String rawPassword, Set<String> roleNames) {
		String trimmed = email == null ? "" : email.trim();
		if (!StringUtils.hasText(trimmed)) {
			throw new IllegalArgumentException("Email is required.");
		}
		if (userRepository.findByEmail(trimmed).isPresent()) {
			throw new IllegalArgumentException("This email is already in use.");
		}
		User user = new User(trimmed, passwordEncoder.encode(rawPassword));
		user.setEnabled(true);
		user.replaceRoles(resolveRoles(roleNames));
		return userRepository.save(user);
	}

	@Transactional
	public void updateUser(UUID id, boolean enabled, Set<String> roleNames, String newRawPassword) {
		User user = getUserById(id);
		user.setEnabled(enabled);
		user.replaceRoles(resolveRoles(roleNames));
		if (StringUtils.hasText(newRawPassword)) {
			user.setPasswordHash(passwordEncoder.encode(newRawPassword.trim()));
		}
		userRepository.save(user);
	}

	private Set<Role> resolveRoles(Set<String> roleNames) {
		if (roleNames == null || roleNames.isEmpty()) {
			throw new IllegalArgumentException("Select at least one role.");
		}
		var names = roleNames.stream().map(String::trim).filter(StringUtils::hasText).collect(Collectors.toSet());
		if (names.isEmpty()) {
			throw new IllegalArgumentException("Select at least one role.");
		}
		var resolved = new HashSet<Role>();
		for (String name : names) {
			var role = roleRepository.findByName(name)
				.orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name));
			resolved.add(role);
		}
		return resolved;
	}
}
