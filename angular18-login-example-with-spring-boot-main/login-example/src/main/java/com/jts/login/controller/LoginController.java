package com.jts.login.controller;

import com.jts.login.config.JWTService;
import com.jts.login.dto.*;
import com.jts.login.repo.LoginRepository;
import com.jts.login.service.EmailService;
import com.jts.login.service.LoginService;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	private final LoginRepository loginRepository;
	private final JWTService jwtService;
	private final EmailService emailService;
	private final AuthenticationManager authenticationManager;
	private final LoginService loginService;
	private final PasswordEncoder passwordEncoder;
	private final String frontendResetUrl;

	public LoginController(
			LoginRepository loginRepository,
			JWTService jwtService,
			EmailService emailService,
			AuthenticationManager authenticationManager,
			LoginService loginService,
			PasswordEncoder passwordEncoder,
			@Value("${frontend.reset-url}") String frontendResetUrl) {
		this.loginRepository = loginRepository;
		this.jwtService = jwtService;
		this.emailService = emailService;
		this.authenticationManager = authenticationManager;
		this.loginService = loginService;
		this.passwordEncoder = passwordEncoder;
		this.frontendResetUrl = frontendResetUrl;
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		try {
			// Validate email format first
			if (request.getEmail() == null || !request.getEmail().contains("@")) {
				return ResponseEntity.badRequest().body(Map.of(
						"error", "Invalid email address format"
				));
			}

			User user = loginRepository.findByEmail(request.getEmail().trim())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));

			String resetToken = jwtService.generatePasswordResetToken(user.getEmail());
			user.setResetToken(resetToken);
			user.setTokenExpiry(LocalDateTime.now().plusHours(24));
			loginRepository.save(user);

			String resetLink = frontendResetUrl + "?token=" + resetToken;

			try {
				emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
				return ResponseEntity.ok(Map.of(
						"message", "Password reset link has been sent to your email",
						"status", "success"
				));
			} catch (Exception e) {
				logger.error("Failed to send reset email to {}", user.getEmail(), e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body(Map.of("error", "Failed to send reset email. Please try again later."));
			}

		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Password reset failed for email: {}", request.getEmail(), e);
			return ResponseEntity.internalServerError().body(Map.of(
					"error", "An error occurred. Please try again later."
			));
		}
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
		try {
			// First validate the token structure
			if (request.getToken() == null || request.getToken().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
			}

			// Extract email from token with proper error handling
			String email;
			try {
				email = jwtService.extractEmail(request.getToken());
			} catch (ExpiredJwtException ex) {
				logger.warn("Expired token attempt: {}", ex.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Reset link has expired. Please request a new one."));
			} catch (Exception ex) {
				logger.warn("Invalid token format: {}", ex.getMessage());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Invalid reset token."));
			}

			// Find user and validate
			User user = loginRepository.findByEmail(email)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

			// Additional validation
			if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(LocalDateTime.now())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Reset link has expired. Please request a new one."));
			}

			if (!user.getResetToken().equals(request.getToken())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Invalid reset token."));
			}

			// Validate new password
			if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Password must be at least 8 characters long."));
			}

			// Update user
			user.setPassword(passwordEncoder.encode(request.getNewPassword()));
			user.setResetToken(null);
			user.setTokenExpiry(null);
			loginRepository.save(user);

			return ResponseEntity.ok(Map.of(
					"message", "Password reset successfully",
					"status", "success"
			));

		} catch (ResponseStatusException ex) {
			throw ex; // Re-throw explicit exceptions
		} catch (Exception e) {
			logger.error("Password reset failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "An unexpected error occurred. Please try again."));
		}
	}

	@PostMapping("/doLogin")
	public ResponseEntity<LoginResponse> doLogin(@RequestBody LoginRequest request) {
		LoginResponse response = new LoginResponse();

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		if (authentication.isAuthenticated()) {
			response.setToken(jwtService.generateToken(request.getEmail()));
		}

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@GetMapping("/dashboard")
	public ResponseEntity<DashboardResponse> dashboard() {
		DashboardResponse response = new DashboardResponse();
		response.setResponse("Success");

		System.out.println("Dashboard Response");

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/doRegister")
	public ResponseEntity<SignupResponse> doRegister(@RequestBody SignupRequest request) {
		return new ResponseEntity<>(loginService.doRegister(request), HttpStatus.CREATED);
	}
}
