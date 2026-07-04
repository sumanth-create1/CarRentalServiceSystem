package com.carrental.config;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		boolean isUser = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"));

		if (isAdmin) {
			response.sendRedirect("/admin/");
		} else if (isUser) {
			response.sendRedirect("/user/home");
		} else {
			response.sendRedirect("/");
		}
	}

}
