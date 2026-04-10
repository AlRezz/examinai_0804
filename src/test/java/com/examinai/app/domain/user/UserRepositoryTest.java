package com.examinai.app.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.examinai.app.config.PasswordEncoderConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
@Import(PasswordEncoderConfig.class)
class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void liquibaseSeedsRolesAndBootstrapAdmin() {
		assertThat(roleRepository.count()).isEqualTo(4);
		assertThat(roleRepository.findByName("administrator")).isPresent();
		assertThat(roleRepository.findByName("intern")).isPresent();

		var admin = userRepository.findByEmail("admin@examinai.local");
		assertThat(admin).isPresent();
		assertThat(passwordEncoder.matches("ChangeMe!Dev1", admin.get().getPasswordHash())).isTrue();
		assertThat(admin.get().getRoles()).extracting(Role::getName).containsExactly("administrator");
		assertThat(admin.get().isEnabled()).isTrue();
	}

	@Test
	void findWithRolesById_initializesRolesCollection() {
		var id = userRepository.findByEmail("admin@examinai.local").orElseThrow().getId();
		User admin = userRepository.findWithRolesById(id).orElseThrow();
		assertThat(admin.getRoles()).extracting(Role::getName).containsExactly("administrator");
	}

	@Test
	void persistsNewUserWithBcryptEncodedPassword() {
		Role intern = roleRepository.findByName("intern").orElseThrow();
		User user = new User("intern1@examinai.local", passwordEncoder.encode("secret"));
		user.addRole(intern);
		userRepository.saveAndFlush(user);

		var loaded = userRepository.findByEmail("intern1@examinai.local").orElseThrow();
		assertThat(passwordEncoder.matches("secret", loaded.getPasswordHash())).isTrue();
		assertThat(loaded.getRoles()).extracting(Role::getName).containsExactly("intern");
	}
}
