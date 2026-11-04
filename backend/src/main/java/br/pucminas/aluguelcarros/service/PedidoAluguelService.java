package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.pedido.AvaliacaoRequest;
import br.pucminas.aluguelcarros.dto.pedido.PedidoRequest;
import br.pucminas.aluguelcarros.dto.pedido.PedidoResponse;
import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.exception.ResourceNotFoundException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.Perfil;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import br.pucminas.aluguelcarros.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Pedidos de aluguel (RN08–RN15, RN21).
 * As transições de status ficam em PedidoAluguel; aqui ficam permissão, busca e disponibilidade do carro.
 */
@Service
@RequiredArgsConstructor
public class PedidoAluguelService {

    // RN21: pedidos nesses status "ocupam" o carro no período
    private static final List<StatusPedido> STATUS_QUE_RESERVAM = List.of(StatusPedido.APROVADO, StatusPedido.CONTRATADO);

    private final PedidoAluguelRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final AutomovelRepository automovelRepository;
    private final AgenteRepository agenteRepository;

    /** RN08: só o cliente introduz; o pedido nasce EM_ANALISE. */
    @Transactional
    public PedidoResponse criar(Long clienteId, PedidoRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
        Automovel automovel = buscarAutomovel(request.automovelId());

        PedidoAluguel pedido = new PedidoAluguel(cliente, automovel, request.dataInicio(), request.dataFim());
        validarDisponibilidade(pedido);
        return PedidoResponse.from(pedidoRepository.save(pedido));
    }

    /** RN09: o cliente vê só os próprios pedidos. */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarDoCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByDataCriacaoDesc(clienteId).stream()
                .map(PedidoResponse::from)
                .toList();
    }

    /** Agentes: todos os pedidos, ou filtrados por status (US13). */
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(StatusPedido status) {
        List<PedidoAluguel> pedidos = status == null
                ? pedidoRepository.findAllByOrderByDataCriacaoDesc()
                : pedidoRepository.findByStatusOrderByDataCriacaoAsc(status);
        return pedidos.stream().map(PedidoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id, UsuarioAutenticado usuario) {
        return PedidoResponse.from(buscarVisivel(id, usuario));
    }

    /** RN10 / RN14: cliente (só os seus) ou agente modificam enquanto estiver EM_ANALISE. */
    @Transactional
    public PedidoResponse modificar(Long id, PedidoRequest request, UsuarioAutenticado usuario) {
        PedidoAluguel pedido = buscarVisivel(id, usuario);
        Automovel automovel = buscarAutomovel(request.automovelId());

        pedido.modificar(request.dataInicio(), request.dataFim(), automovel);
        validarDisponibilidade(pedido);
        return PedidoResponse.from(pedido);
    }

    /** RN11: o cliente cancela os próprios pedidos EM_ANALISE ou APROVADO. */
    @Transactional
    public PedidoResponse cancelar(Long id, UsuarioAutenticado usuario) {
        PedidoAluguel pedido = buscarVisivel(id, usuario);
        pedido.cancelar();
        return PedidoResponse.from(pedido);
    }

    /** RN15: parecer positivo → APROVADO, negativo → REPROVADO; registra parecer e agente. */
    @Transactional
    public PedidoResponse avaliar(Long id, AvaliacaoRequest request, Long agenteId) {
        PedidoAluguel pedido = buscarEntidade(id);
        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Agente", agenteId));

        if (request.aprovado()) {
            // Confere antes de mudar o status, senão a consulta contaria o próprio pedido como APROVADO
            validarDisponibilidade(pedido);
            pedido.aprovar(agente, request.parecer());
        } else {
            pedido.reprovar(agente, request.parecer());
        }
        return PedidoResponse.from(pedido);
    }

    /** Cliente só enxerga os próprios pedidos (404 para não revelar que o pedido existe); agentes enxergam todos. */
    private PedidoAluguel buscarVisivel(Long id, UsuarioAutenticado usuario) {
        PedidoAluguel pedido = buscarEntidade(id);
        if (usuario.perfil() == Perfil.CLIENTE && !pedido.getCliente().getId().equals(usuario.id())) {
            throw new ResourceNotFoundException("Pedido", id);
        }
        return pedido;
    }

    private PedidoAluguel buscarEntidade(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    private Automovel buscarAutomovel(Long id) {
        return automovelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automóvel", id));
    }

    // RN21: o carro não pode estar aprovado/contratado para outro pedido no mesmo período
    private void validarDisponibilidade(PedidoAluguel pedido) {
        boolean ocupado = pedidoRepository.existsConflitoDePeriodo(
                pedido.getAutomovel().getId(), pedido.getDataInicio(), pedido.getDataFim(), STATUS_QUE_RESERVAM);
        if (ocupado) {
            throw new BusinessException("O automóvel já está reservado em parte desse período.");
        }
    }
}
