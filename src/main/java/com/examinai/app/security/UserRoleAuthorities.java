package com.examinai.app.security;

import java.util.Locale;

public final class UserRoleAuthorities {

	private UserRoleAuthorities() {
	}

	public static String toAuthority(String roleName) {
		return "ROLE_" + roleName.toUpperCase(Locale.ROOT);
	}
}
