package com.jts.login.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
	private String email;  // Changed from username to email
	private String password;
}