package com.jts.login.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String name;

	private String address;
	private String mobileNo;

	@Column(nullable = false, unique = true)
	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	private String email;

	private String password;
	private String resetToken;
	private LocalDateTime tokenExpiry;

	// Removed username field as we're using email instead
}