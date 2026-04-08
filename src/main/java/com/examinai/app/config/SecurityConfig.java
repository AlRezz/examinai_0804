package com.examinai.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.examinai.app.security.RoleBasedAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final RoleBasedAuthenticationSuccessHandler authenticationSuccessHandler;

	public SecurityConfig(RoleBasedAuthenticationSuccessHandler authenticationSuccessHandler) {
		this.authenticationSuccessHandler = authenticationSuccessHandler;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/", "/login", "/error")
			.permitAll()
			.requestMatchers("/css/**")
			.permitAll()
			.requestMatchers("/actuator/health", "/actuator/health/**")
			.permitAll()
			.requestMatchers("/admin/**")
			.hasRole("ADMINISTRATOR")
			.anyRequest()
			.authenticated())
			.formLogin(form -> form.loginPage("/login")
				.permitAll()
				.successHandler(authenticationSuccessHandler))
			.logout(logout -> logout.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout")
				.invalidateHttpSession(true)
				.deleteCookies("JSESSIONID")
				.permitAll());
		return http.build();
	}

}
