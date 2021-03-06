package es.neifi.controlfitAPI.rest.security.jwt;

import java.security.Key;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import es.neifi.controlfitAPI.rest.model.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.java.Log;

@Log
@Component
public class JwtProvider {

	public static final String TOKEN_HEADER = "Authorization";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String TOKEN_TYPE = "JWT";


	private final String jwtSecret  = "EnUnLugarDeLaManchaDeCuyoNombreNoQuieroAcordarmeNoHaMuchoTiempoQueViviaUnHidalgo";
	@Value("${jwt.token-expiration: 3600}")
	private int jwtDurationTokenSecs;
		
	private  final Key secret = Keys.hmacShaKeyFor(jwtSecret.getBytes());
	private  final byte[] secretBytes = secret.getEncoded();
	private  final String base64SecretBytes = Base64.getEncoder().encodeToString(secretBytes);
	
	
	
	public String generateToken(Authentication authentication) {
		Usuario usuario = (Usuario) authentication.getPrincipal();
		Date tokenExpirationDate = new Date(System.currentTimeMillis() + (jwtDurationTokenSecs * 1000));
		
		return Jwts.builder()
				.setHeaderParam("typ", TOKEN_TYPE).setSubject(Integer.toString(usuario.getId_usuario()))
				.setIssuedAt(new Date()).setExpiration(tokenExpirationDate).claim("fullname", usuario.getUsername())
				.signWith(SignatureAlgorithm.HS512,base64SecretBytes)
				.claim("roles", usuario.getRol().toString()).compact();
	}

	public int getUserIdFromJWT(String token) {
		Claims claims = Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes())).parseClaimsJws(token)
				.getBody();
		return Integer.parseInt(claims.getSubject());
	}

public boolean validateToken(String authToken) {
		
		try {
			Jwts.parser().setSigningKey(jwtSecret.getBytes()).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException ex) {
			log.info("Error en la firma del token JWT: " + ex.getMessage());
		} catch (MalformedJwtException ex) {
			log.info("Token malformado: " + ex.getMessage());
		} catch (ExpiredJwtException ex) {
			log.info("El token ha expirado: " + ex.getMessage());
		} catch (UnsupportedJwtException ex) {
			log.info("Token JWT no soportado: " + ex.getMessage());
		} catch (IllegalArgumentException ex) {
			log.info("JWT claims vacío");
		}
        return false;
		
	}
}
