package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.auth.LoginRequest;
import br.pucminas.aluguelcarros.dto.auth.LoginResponse;
import br.pucminas.aluguelcarros.dto.cliente.ClienteRequest;
import br.pucminas.aluguelcarros.dto.cliente.ClienteResponse;
import br.pucminas.aluguelcarros.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Rotas públicas (liberadas no SecurityConfig). */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // UC01 / US01
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrar(@RequestBody @Valid ClienteRequest request) {
        return authService.registrar(request);
    }

    // UC02 / US02
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }
}
