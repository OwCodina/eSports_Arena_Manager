package com.esports.msvc_auth.security;

import com.esports.msvc_auth.models.AuthUser;
import com.esports.msvc_auth.models.Rol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Genera (FIRMA) el JWT que el resto del sistema validara.
 *
 * El claim "roles" contiene la lista de nombres de todos los roles de la cuenta
 * (ej: ["ROLE_ADMINISTRADOR", "ROLE_ORGANIZADOR"]). El JwtAuthenticationConverter
 * del gateway y de msvc-user los lee y los convierte en authorities.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.expiration-minutes:60}")
    private long expirationMinutes;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(AuthUser cuenta) {
        Instant ahora = Instant.now();
        List<String> roles = cuenta.getRoles().stream().map(Rol::getNombre).toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("msvc-auth")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(cuenta.getEmail())
                .claim("email", cuenta.getEmail())
                .claim("roles", roles)
                .claim("estado", cuenta.getEstado())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
