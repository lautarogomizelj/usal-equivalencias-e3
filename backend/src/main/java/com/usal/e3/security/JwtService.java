package com.usal.e3.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

/** El JWT solo lleva el id de la sesión: los datos y permisos siempre salen de la base. */
@Service
public class JwtService {

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;

    public JwtService(@Value("${e3.sesion.jwt-secret}") String secreto) {
        SecretKey clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        this.decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    public String firmar(UUID sesionId, OffsetDateTime expiraEn) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(sesionId.toString())
                .issuedAt(OffsetDateTime.now().toInstant())
                .expiresAt(expiraEn.toInstant())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** Vacío si el token está manipulado o vencido. */
    public Optional<UUID> leer(String token) {
        try {
            return Optional.of(UUID.fromString(decoder.decode(token).getSubject()));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
