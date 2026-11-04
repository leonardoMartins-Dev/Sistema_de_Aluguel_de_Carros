package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.contrato.ContratoCreditoRequest;
import br.pucminas.aluguelcarros.dto.contrato.ContratoRequest;
import br.pucminas.aluguelcarros.dto.contrato.ContratoResponse;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import br.pucminas.aluguelcarros.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** EMPRESA ou BANCO; o crédito é só para BANCO (SecurityConfig). */
@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping
    public List<ContratoResponse> listar() {
        return contratoService.listar();
    }

    @GetMapping("/{id}")
    public ContratoResponse buscar(@PathVariable Long id) {
        return contratoService.buscar(id);
    }

    // US16
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse executar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                     @RequestBody @Valid ContratoRequest request) {
        return contratoService.executar(request, usuario.id());
    }

    // US17
    @PostMapping("/{id}/credito")
    @ResponseStatus(HttpStatus.CREATED)
    public ContratoResponse concederCredito(@PathVariable Long id,
                                            @AuthenticationPrincipal UsuarioAutenticado usuario,
                                            @RequestBody @Valid ContratoCreditoRequest request) {
        return contratoService.concederCredito(id, request, usuario.id());
    }
}
