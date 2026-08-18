package org.example.springtestweb.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

	/**
	 * Signing key used for HMAC signing of JWTs.
	 * <p>
	 * The key is derived from a secret configured in application properties
	 * (`jwt.secret`). For production, this secret MUST be a sufficiently long
	 * random value stored in a secure store (e.g. Vault, Azure Key Vault).
	 */
	private final Key key;

	/**
	 * Token expiration in milliseconds. Configurable via `jwt.expiration-ms`.
	 */
	private final long expirationMillis;

	/**
	 * Construct the JwtUtil component.
	 *
	 * @param secret           the raw secret used to derive the HMAC signing key.
	 *                         Defaults
	 *                         to a placeholder value — replace in production.
	 * @param expirationMillis token lifetime in milliseconds (defaults to 3600000
	 *                         ms = 1h)
	 *                         — choose an appropriate TTL for your security model.
	 */
	public JwtUtil(@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-ms}") long expirationMillis) {
		// Note: io.jsonwebtoken.security.Keys.hmacShaKeyFor validates the key length.
		// If `secret.getBytes()` is too short for the requested algorithm a runtime
		// IllegalArgumentException will be thrown. Prefer long random secrets (256+
		// bits).
		this.key = Keys.hmacShaKeyFor(secret.getBytes());
		this.expirationMillis = expirationMillis;
	}

	/**
	 * Generate a compact JWT for the given user id and roles.
	 *
	 * The token includes:
	 * - `sub` (subject) set to the user's id
	 * - `iat` (issued at) and `exp` (expiration) timestamps
	 * - a `roles` claim containing the granted roles as a list
	 *
	 * Important notes:
	 * - Keep the token payload minimal: do not store secrets or sensitive PII.
	 * - Use HTTPS for transport to avoid token interception.
	 *
	 * @param userId the database id of the authenticated user
	 * @param roles  the granted roles/authorities to include in the token
	 * @return a signed, compact JWT string
	 */
	public String generateToken(Long userId, List<String> roles) {
		long now = System.currentTimeMillis();
		JwtBuilder builder = Jwts.builder()
				.setSubject(String.valueOf(userId))
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + expirationMillis))
				.claim("roles", roles);
		builder.signWith(key, SignatureAlgorithm.HS256);
		return builder.compact();
	}

	/**
	 * Parse and validate a JWT, returning the JWS claims if the signature and
	 * standard validations pass.
	 *
	 * This method will throw a `JwtException` (or a subclass) if the token is
	 * malformed, expired, or the signature does not match.
	 *
	 * @param token compact JWT string
	 * @return parsed JWS with claims
	 * @throws JwtException on parse/validation failure
	 */
	public Jws<Claims> parseToken(String token) throws JwtException {
		return Jwts.parser().verifyWith((SecretKey) key).build().parseSignedClaims(token);
	}

	/**
	 * Quick boolean check whether a token is valid. This wraps {@link #parseToken}
	 * and converts parse exceptions into a boolean result.
	 *
	 * Use {@link #parseToken} when you need the claims or want to distinguish
	 * different failure reasons.
	 *
	 * @param token compact JWT string
	 * @return true if token is well-formed, signature-valid and not expired
	 */
	public boolean isTokenValid(String token) {
		try {
			parseToken(token);
			return true;
		} catch (JwtException ex) {
			// Validation failure (expired, invalid signature, malformed, etc.)
			return false;
		}
	}

	/**
	 * Convenience helper to extract the claims map from a token. Throws the same
	 * exceptions as {@link #parseToken} when the token is invalid.
	 *
	 * @param token compact JWT string
	 * @return claims map (contains standard claims like `sub`, `exp` and any
	 *         custom claims such as `roles`)
	 */
	public Map<String, Object> getClaims(String token) {
		Jws<Claims> jws = parseToken(token);
		return jws.getBody();
	}
}
