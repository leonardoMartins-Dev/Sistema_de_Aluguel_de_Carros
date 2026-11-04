package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.cliente.ClienteResponse;
import br.pucminas.aluguelcarros.dto.cliente.ClienteUpdateRequest;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import br.pucminas.aluguelcarros.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Perfil CLIENTE (SecurityConfig). Sempre opera sobre o cliente logado (RN07). */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // US04
    @GetMapping("/me")
    public ClienteResponse buscar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return clienteService.buscar(usuario.id());
    }

    // US05
    @PutMapping("/me")
    public ClienteResponse atualizar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                     @RequestBody @Valid ClienteUpdateRequest request) {
        return clienteService.atualizar(usuario.id(), request);
    }

    // US06
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        clienteService.excluir(usuario.id());
    }
}
