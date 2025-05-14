package com.jts.login.config;

import com.jts.login.dto.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserInfoUserDetails implements UserDetails {

	private final String email;  // Changed from username to email
	private final String password;
	private final Collection<? extends GrantedAuthority> authorities;

	public UserInfoUserDetails(User user) {
		this.email = user.getEmail();  // Changed to use email
		this.password = user.getPassword();
		this.authorities = Collections.singletonList(
				new SimpleGrantedAuthority("ROLE_USER")
		);
	}

	@Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
	@Override public String getPassword() { return password; }
	@Override public String getUsername() { return email; }  // Return email as username
	@Override public boolean isAccountNonExpired() { return true; }
	@Override public boolean isAccountNonLocked() { return true; }
	@Override public boolean isCredentialsNonExpired() { return true; }
	@Override public boolean isEnabled() { return true; }
}