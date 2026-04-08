package com.examinai.app.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleBasedAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final String ADMIN_HOME = "/admin/users";
	private static final String DEFAULT_HOME = "/home";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		var target = authentication.getAuthorities()
			.stream()
			.anyMatch(a -> UserRoleAuthorities.toAuthority("administrator").equals(a.getAuthority())) ? ADMIN_HOME
					: DEFAULT_HOME;
		getRedirectStrategy().sendRedirect(request, response, target);
	}
}
