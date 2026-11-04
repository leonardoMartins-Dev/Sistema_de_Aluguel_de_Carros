package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.cliente.ClienteResponse;
import br.pucminas.aluguelcarros.dto.cliente.ClienteUpdateRequest;
import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.exception.ResourceNotFoundException;
import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import br.pucminas.aluguelcarros.repository.ClienteRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Dados do próprio cliente (US04–US06).
 * RN07: sempre recebe o id do cliente logado, então ele só mexe nos próprios dados.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoAluguelRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Long clienteId) {
        return ClienteResponse.from(buscarEntidade(clienteId));
    }

    @Transactional
    public ClienteResponse atualizar(Long clienteId, ClienteUpdateRequest request) {
        Cliente cliente = buscarEntidade(clienteId);

        // RN06: CPF e RG continuam únicos
        if (clienteRepository.existsByCpfAndIdNot(request.cpf(), clienteId)) {
            throw new BusinessException("Já existe um cliente cadastrado com este CPF.");
        }
        if (clienteRepository.existsByRgAndIdNot(request.rg(), clienteId)) {
            throw new BusinessException("Já existe um cliente cadastrado com este RG.");
        }

        request.copiarPara(cliente);
        if (request.senha() != null) {
            cliente.setSenha(passwordEncoder.encode(request.senha()));
        }
        return ClienteResponse.from(cliente);
    }

    @Transactional
    public void excluir(Long clienteId) {
        Cliente cliente = buscarEntidade(clienteId);

        // RN22: com contrato executado não pode excluir
        if (pedidoRepository.existsByClienteIdAndStatus(clienteId, StatusPedido.CONTRATADO)) {
            throw new BusinessException("Não é possível excluir a conta: existe pedido com contrato executado.");
        }

        // Os demais pedidos (em análise, aprovados, reprovados, cancelados) não têm contrato e saem junto
        pedidoRepository.deleteByClienteId(clienteId);
        clienteRepository.delete(cliente);
    }

    private Cliente buscarEntidade(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
    }
}
