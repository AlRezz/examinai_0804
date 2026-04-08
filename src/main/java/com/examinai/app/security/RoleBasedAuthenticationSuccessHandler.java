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

	private static final String MENTOR_HOME = "/tasks";

	private static final String INTERN_HOME = "/intern/tasks";

	private static final String COORDINATOR_HOME = "/coordinator";

	private static final String DEFAULT_HOME = "/home";

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		var authorities = authentication.getAuthorities();
		String target = DEFAULT_HOME;
		if (authorities.stream().anyMatch(a -> UserRoleAuthorities.toAuthority("administrator").equals(a.getAuthority()))) {
			target = ADMIN_HOME;
		}
		else if (authorities.stream().anyMatch(a -> UserRoleAuthorities.toAuthority("mentor").equals(a.getAuthority()))) {
			target = MENTOR_HOME;
		}
		else if (authorities.stream().anyMatch(a -> UserRoleAuthorities.toAuthority("coordinator").equals(a.getAuthority()))) {
			target = COORDINATOR_HOME;
		}
		else if (authorities.stream().anyMatch(a -> UserRoleAuthorities.toAuthority("intern").equals(a.getAuthority()))) {
			target = INTERN_HOME;
		}
		getRedirectStrategy().sendRedirect(request, response, target);
	}
}
