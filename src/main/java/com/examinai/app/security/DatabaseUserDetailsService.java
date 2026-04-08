package com.examinai.app.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examinai.app.domain.user.UserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public DatabaseUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var user = userRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
		var authorities = user.getRoles()
			.stream()
			.map(r -> new SimpleGrantedAuthority(UserRoleAuthorities.toAuthority(r.getName())))
			.collect(Collectors.toSet());
		return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
			.password(user.getPasswordHash())
			.disabled(!user.isEnabled())
			.authorities(authorities)
			.build();
	}
}
