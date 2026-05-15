package org.example.springtestweb.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

	private final Key key;
	private final long expirationMillis;

	public JwtUtil(@Value("${jwt.secret:defaultsecretchangeme}") String secret,
			@Value("${jwt.expiration-ms:3600000}") long expirationMillis) {
		// Use the provided secret to create a signing key. For production use a strong
		// random secret stored securely.
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMillis = expirationMillis;
	}

	public String generateToken(Long userId, List<String> roles) {
		long now = System.currentTimeMillis();
		JwtBuilder builder = Jwts.builder()
				.setSubject(String.valueOf(userId))
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + expirationMillis))
				.claim("roles", roles)
				.signWith(key, SignatureAlgorithm.HS256);
		return builder.compact();
	}

	public Jws<Claims> parseToken(String token) throws JwtException {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
	}

	public boolean isTokenValid(String token) {
		try {
			parseToken(token);
			return true;
		} catch (JwtException ex) {
			return false;
		}
	}

	public Map<String, Object> getClaims(String token) {
		Jws<Claims> jws = parseToken(token);
		return jws.getBody();
	}
}
