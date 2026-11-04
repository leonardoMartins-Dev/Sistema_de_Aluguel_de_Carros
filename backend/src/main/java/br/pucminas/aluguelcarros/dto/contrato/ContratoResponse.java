package br.pucminas.aluguelcarros.dto.contrato;

import br.pucminas.aluguelcarros.dto.automovel.AutomovelResponse;
import br.pucminas.aluguelcarros.model.Contrato;
import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.TipoContrato;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoResponse(
        Long id,
        TipoContrato tipo,
        LocalDate dataAssinatura,
        BigDecimal valorTotal,
        Long pedidoId,
        String clienteNome,
        AutomovelResponse automovel,
        Long agenteResponsavelId,
        String agenteResponsavelNome,
        ContratoCreditoResponse credito
) {
    public static ContratoResponse from(Contrato c) {
        PedidoAluguel pedido = c.getPedido();
        return new ContratoResponse(
                c.getId(),
                c.getTipo(),
                c.getDataAssinatura(),
                c.getValorTotal(),
                pedido.getId(),
                pedido.getCliente().getNome(),
                AutomovelResponse.from(pedido.getAutomovel()),
                c.getAgenteResponsavel().getId(),
                c.getAgenteResponsavel().getNome(),
                c.possuiCredito() ? ContratoCreditoResponse.from(c.getContratoCredito()) : null
        );
    }
}
