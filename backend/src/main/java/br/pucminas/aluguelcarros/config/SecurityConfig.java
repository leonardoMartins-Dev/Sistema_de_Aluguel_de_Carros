package br.pucminas.aluguelcarros.config;

import br.pucminas.aluguelcarros.security.JwtAuthenticationFilter;
import br.pucminas.aluguelcarros.security.JwtService;
import br.pucminas.aluguelcarros.security.SecurityErrorHandler;
import br.pucminas.aluguelcarros.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * RN01: tudo exige token, menos cadastro e login.
 * RN03: as rotas são liberadas por perfil, conforme a tabela de endpoints do MODELAGEM.md (5.4).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] AGENTES = {"EMPRESA", "BANCO"};

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final SecurityErrorHandler securityErrorHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // API stateless com JWT no header: não usa cookie de sessão, então CSRF não se aplica
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/health", "/error").permitAll()

                        // Cliente: só os próprios dados (RN07)
                        .requestMatchers("/api/clientes/**").hasRole("CLIENTE")

                        // Automóveis: todos consultam, só agentes mantêm (US07, US12)
                        .requestMatchers(HttpMethod.GET, "/api/automoveis/**").authenticated()
                        .requestMatchers("/api/automoveis/**").hasAnyRole(AGENTES)

                        // Pedidos (RN08–RN15)
                        .requestMatchers(HttpMethod.POST, "/api/pedidos").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/meus").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/cancelar").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/avaliar").hasAnyRole(AGENTES)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos").hasAnyRole(AGENTES)

                        // Contratos: agentes executam, só banco concede crédito (RN16–RN19)
                        .requestMatchers(HttpMethod.POST, "/api/contratos/*/credito").hasRole("BANCO")
                        .requestMatchers("/api/contratos/**").hasAnyRole(AGENTES)

                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(securityErrorHandler)
                        .accessDeniedHandler(securityErrorHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, usuarioDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // RN02: senha guardada com BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Usa o UsuarioDetailsService + PasswordEncoder acima para conferir login e senha
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Aceita mais de uma origem separada por vírgula em FRONTEND_URL
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}") String origens) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(origens.split(",")).map(String::trim).toList());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
