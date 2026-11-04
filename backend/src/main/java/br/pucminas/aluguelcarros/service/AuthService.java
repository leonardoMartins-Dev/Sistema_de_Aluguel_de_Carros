package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.auth.LoginRequest;
import br.pucminas.aluguelcarros.dto.auth.LoginResponse;
import br.pucminas.aluguelcarros.dto.cliente.ClienteRequest;
import br.pucminas.aluguelcarros.dto.cliente.ClienteResponse;
import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import br.pucminas.aluguelcarros.security.JwtService;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Login (US02) e cadastro público do cliente (US01). */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    /** Login ou senha errados → BadCredentialsException → 401 no GlobalExceptionHandler. */
    public LoginResponse login(LoginRequest request) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.senha()));

        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacao.getPrincipal();
        String token = jwtService.gerarToken(usuario);

        return new LoginResponse(token, usuario.id(), usuario.login(), usuario.nome(), usuario.perfil());
    }

    @Transactional
    public ClienteResponse registrar(ClienteRequest request) {
        // RN02 e RN06: login, CPF e RG únicos
        if (usuarioRepository.existsByLogin(request.login())) {
            throw new BusinessException("Já existe um usuário com este login.");
        }
        if (clienteRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("Já existe um cliente cadastrado com este CPF.");
        }
        if (clienteRepository.existsByRg(request.rg())) {
            throw new BusinessException("Já existe um cliente cadastrado com este RG.");
        }

        Cliente cliente = request.toEntity(passwordEncoder.encode(request.senha()));
        return ClienteResponse.from(clienteRepository.save(cliente));
    }
}
