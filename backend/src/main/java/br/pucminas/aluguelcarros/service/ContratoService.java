package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.contrato.ContratoCreditoRequest;
import br.pucminas.aluguelcarros.dto.contrato.ContratoRequest;
import br.pucminas.aluguelcarros.dto.contrato.ContratoResponse;
import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.exception.ResourceNotFoundException;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.ContratoCredito;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.TipoContrato;
import br.pucminas.aluguelcarros.repository.AgenteRepository;
import br.pucminas.aluguelcarros.repository.BancoRepository;
import br.pucminas.aluguelcarros.repository.ContratoRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Execução do contrato (RN16–RN18) e concessão de crédito (RN19). */
@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final PedidoAluguelRepository pedidoRepository;
    private final AgenteRepository agenteRepository;
    private final BancoRepository bancoRepository;

    @Transactional(readOnly = true)
    public List<ContratoResponse> listar() {
        return contratoRepository.findAllByOrderByIdDesc().stream()
                .map(ContratoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContratoResponse buscar(Long id) {
        return ContratoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ContratoResponse executar(ContratoRequest request, Long agenteId) {
        PedidoAluguel pedido = pedidoRepository.findById(request.pedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", request.pedidoId()));

        // RN17: no máximo um contrato por pedido
        if (contratoRepository.existsByPedidoId(pedido.getId())) {
            throw new BusinessException("Este pedido já possui contrato.");
        }

        Agente agente = agenteRepository.findById(agenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Agente", agenteId));

        // RN16: só pedido APROVADO; passa a CONTRATADO
        pedido.marcarComoContratado();
        Contrato contrato = new Contrato(pedido, agente, request.tipo(), request.valorTotal());

        // RN18: registra o proprietário do automóvel conforme o tipo
        pedido.getAutomovel().registrarPropriedade(contrato.definirProprietario());

        return ContratoResponse.from(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponse concederCredito(Long contratoId, ContratoCreditoRequest request, Long bancoId) {
        Contrato contrato = buscarEntidade(contratoId);
        Banco banco = bancoRepository.findById(bancoId)
                .orElseThrow(() -> new ResourceNotFoundException("Banco", bancoId));

        contrato.associarCredito(new ContratoCredito(
                banco, request.valor(), request.taxaJurosMensal(), request.numeroParcelas()));

        // RN18: se o carro fica com o banco, quem concedeu o crédito passa a ser o proprietário
        if (contrato.getTipo() == TipoContrato.PROPRIEDADE_BANCO) {
            contrato.getPedido().getAutomovel().registrarPropriedade(contrato.definirProprietario());
        }

        // flush para o crédito já voltar com id na resposta
        return ContratoResponse.from(contratoRepository.saveAndFlush(contrato));
    }

    private Contrato buscarEntidade(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato", id));
    }
}
