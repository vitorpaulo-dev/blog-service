package dev.vitorpaulo.blog.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthorRoleMapper {

    public static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
    public static final String AUTHOR_AUTHORITY = "ROLE_AUTHOR";
    public static final String ADMIN_CLERK_ROLE = "org:admin";
    public static final String MEMBER_CLERK_ROLE = "org:member";

    public static GrantedAuthority toAuthority(String clerkRole) {
        return new SimpleGrantedAuthority(isAdmin(clerkRole) ? ADMIN_AUTHORITY : AUTHOR_AUTHORITY);
    }

    public static boolean isAdmin(String clerkRole) {
        return ADMIN_CLERK_ROLE.equalsIgnoreCase(clerkRole);
    }
}
