package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.pedido.AvaliacaoRequest;
import br.pucminas.aluguelcarros.dto.pedido.PedidoRequest;
import br.pucminas.aluguelcarros.dto.pedido.PedidoResponse;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import br.pucminas.aluguelcarros.service.PedidoAluguelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** O perfil exigido por cada rota está no SecurityConfig. */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoAluguelController {

    private final PedidoAluguelService pedidoService;

    // US08 (CLIENTE)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                @RequestBody @Valid PedidoRequest request) {
        return pedidoService.criar(usuario.id(), request);
    }

    // US09 (CLIENTE)
    @GetMapping("/meus")
    public List<PedidoResponse> listarMeus(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.listarDoCliente(usuario.id());
    }

    // US13 (EMPRESA, BANCO): /api/pedidos?status=EM_ANALISE; sem status lista todos
    @GetMapping
    public List<PedidoResponse> listar(@RequestParam(required = false) StatusPedido status) {
        return pedidoService.listar(status);
    }

    // Cliente só vê os próprios; agentes veem todos
    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.buscar(id, usuario);
    }

    // US10 / US14 (CLIENTE, EMPRESA, BANCO)
    @PutMapping("/{id}")
    public PedidoResponse modificar(@PathVariable Long id,
                                    @AuthenticationPrincipal UsuarioAutenticado usuario,
                                    @RequestBody @Valid PedidoRequest request) {
        return pedidoService.modificar(id, request, usuario);
    }

    // US11 (CLIENTE)
    @PatchMapping("/{id}/cancelar")
    public PedidoResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pedidoService.cancelar(id, usuario);
    }

    // US15 (EMPRESA, BANCO)
    @PatchMapping("/{id}/avaliar")
    public PedidoResponse avaliar(@PathVariable Long id,
                                  @AuthenticationPrincipal UsuarioAutenticado usuario,
                                  @RequestBody @Valid AvaliacaoRequest request) {
        return pedidoService.avaliar(id, request, usuario.id());
    }
}
