package com.jts.login.config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

	public static final String SECRET = "404D635166546A576E5A7234753778214125442A472D4B6150645267556B5870";

	public String extractEmail(String token) {  // Changed from extractUsername to extractEmail
		return extractClaim(token, Claims::getSubject);
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public Boolean validateToken(String token, String email) {
		final String tokenEmail = extractEmail(token);
		return (email.equals(tokenEmail) && !isTokenExpired(token));
	}
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String email = extractEmail(token);
		return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
	public String generatePasswordResetToken(String email) {
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	public String generateToken(String email) {  // Changed parameter from userName to email
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, email);
	}

	private String createToken(Map<String, Object> claims, String email) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(email)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				// Change from 30 minutes to 24 hours (86400000 ms)
				.setExpiration(new Date(System.currentTimeMillis() + 86400000))
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
	}

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}