package br.pucminas.aluguelcarros.dto.pedido;

import br.pucminas.aluguelcarros.dto.automovel.AutomovelResponse;
import br.pucminas.aluguelcarros.model.Agente;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PedidoResponse(
        Long id,
        StatusPedido status,
        LocalDateTime dataCriacao,
        LocalDate dataInicio,
        LocalDate dataFim,
        String parecer,
        Long clienteId,
        String clienteNome,
        // Base da análise financeira feita pelo agente (RN15)
        String clienteProfissao,
        BigDecimal clienteRendaTotal,
        AutomovelResponse automovel,
        Long agenteAvaliadorId,
        String agenteAvaliadorNome,
        boolean editavel,
        boolean cancelavel
) {
    public static PedidoResponse from(PedidoAluguel p) {
        Agente avaliador = p.getAgenteAvaliador();
        return new PedidoResponse(
                p.getId(),
                p.getStatus(),
                p.getDataCriacao(),
                p.getDataInicio(),
                p.getDataFim(),
                p.getParecer(),
                p.getCliente().getId(),
                p.getCliente().getNome(),
                p.getCliente().getProfissao(),
                p.getCliente().getRendaTotal(),
                AutomovelResponse.from(p.getAutomovel()),
                avaliador != null ? avaliador.getId() : null,
                avaliador != null ? avaliador.getNome() : null,
                p.isEditavel(),
                p.isCancelavel()
        );
    }
}
