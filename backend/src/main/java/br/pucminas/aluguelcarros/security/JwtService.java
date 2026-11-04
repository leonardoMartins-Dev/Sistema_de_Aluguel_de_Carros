package br.pucminas.aluguelcarros.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Gera e valida o token JWT (assinado com HMAC-SHA usando jwt.secret). */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoMs;

    // O secret precisa ter pelo menos 32 caracteres, senão o jjwt recusa a chave na inicialização
    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expiracaoMs) {
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(UsuarioAutenticado usuario) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(usuario.login())
                .claim("id", usuario.id())
                .claim("perfil", usuario.perfil().name())
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    /**
     * Confere assinatura e expiração e devolve o login (subject).
     *
     * @throws JwtException se o token for inválido ou estiver expirado
     */
    public String extrairLogin(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
