package com.jts.login.service;

import com.jts.login.dto.*;
import com.jts.login.repo.LoginRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService {
	private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

	@Autowired
	private LoginRepository loginRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	public void generatePasswordResetToken(String email) throws Exception {
		try {
			Optional<User> userOptional = loginRepository.findByEmail(email);
			if (userOptional.isEmpty()) {
				logger.warn("Password reset requested for non-existent email: {}", email);
				throw new Exception("If this email exists, a reset link has been sent");
			}

			User user = userOptional.get();
			String token = UUID.randomUUID().toString();
			user.setResetToken(token);
			user.setTokenExpiry(LocalDateTime.now().plusHours(1));
			loginRepository.save(user);

			sendResetEmail(user.getEmail(), token); // Make sure this isn't throwing exceptions
			logger.info("Password reset token generated for email: {}", email);
		} catch (MessagingException e) {
			logger.error("Failed to send reset email to: {}", email, e);
			throw new Exception("Failed to send reset email. Please try again later.");
		} catch (Exception e) {
			logger.error("Error generating reset token for email: {}", email, e);
			throw new Exception("Error processing your request. Please try again.");
		}
	}

	public void resetPassword(String token, String newPassword) throws Exception {
		try {
			if (newPassword == null || newPassword.length() < 8) {
				throw new IllegalArgumentException("Password must be at least 8 characters");
			}

			Optional<User> userOptional = loginRepository.findByResetToken(token);
			if (userOptional.isEmpty()) {
				throw new IllegalArgumentException("Invalid or expired reset link");
			}

			User user = userOptional.get();
			if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
				throw new IllegalArgumentException("Reset link has expired");
			}

			user.setPassword(passwordEncoder.encode(newPassword));
			user.setResetToken(null);
			user.setTokenExpiry(null);
			loginRepository.save(user);
			logger.info("Password reset successfully for user: {}", user.getEmail()); // Changed from username to email
		} catch (IllegalArgumentException e) {
			logger.warn("Password reset validation failed: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Error resetting password", e);
			throw new Exception("Error resetting password. Please try again.");
		}
	}

	private void sendResetEmail(String toEmail, String token) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		String resetLink = frontendUrl + "/reset-password?token=" + token;

		helper.setFrom(fromEmail);
		helper.setTo(toEmail);
		helper.setSubject("Password Reset Request");
		helper.setText("Please click the following link to reset your password: " + resetLink +
				"\n\nThis link will expire in 1 hour.");

		mailSender.send(message);
		logger.info("Reset email sent to: {}", toEmail);
	}

	public String doLogin(LoginRequest request) {
		Optional<User> user = loginRepository.findByEmail(request.getEmail()); // Changed to findByEmail
		return user.isPresent() ? "User details found" : "User details not found";
	}

	public SignupResponse doRegister(SignupRequest request) {
		// Validate request
		if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required");
		}

		// Check if email already exists
		if (loginRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new IllegalArgumentException("Email already in use");
		}

		// Create and save user
		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setAddress(request.getAddress());
		user.setMobileNo(request.getMobileNo());

		User savedUser = loginRepository.save(user);

		SignupResponse response = new SignupResponse();
		response.setResponse("User created successfully with ID: " + savedUser.getId());
		return response;
	}
}