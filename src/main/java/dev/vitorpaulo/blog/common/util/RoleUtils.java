package dev.vitorpaulo.blog.common.util;

public final class RoleUtils {

	public static Boolean isAdmin(String role) {
		return "admin".equalsIgnoreCase(role);
	}
}
