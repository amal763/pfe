package com.jts.login.config;

import com.jts.login.dto.User;
import com.jts.login.repo.LoginRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserInfoUserDetailsService implements UserDetailsService {

	private final LoginRepository loginRepository;

	public UserInfoUserDetailsService(LoginRepository loginRepository) {
		this.loginRepository = loginRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {  // Changed parameter from username to email
		Optional<User> user = loginRepository.findByEmail(email);  // Changed to findByEmail
		return user.map(UserInfoUserDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
	}
}