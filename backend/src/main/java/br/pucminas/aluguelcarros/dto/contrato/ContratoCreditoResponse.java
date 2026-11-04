package br.pucminas.aluguelcarros.dto.contrato;

import br.pucminas.aluguelcarros.model.ContratoCredito;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratoCreditoResponse(
        Long id,
        BigDecimal valor,
        BigDecimal taxaJurosMensal,
        Integer numeroParcelas,
        BigDecimal valorParcela,
        LocalDate dataConcessao,
        Long bancoId,
        String bancoNome
) {
    public static ContratoCreditoResponse from(ContratoCredito c) {
        return new ContratoCreditoResponse(
                c.getId(),
                c.getValor(),
                c.getTaxaJurosMensal(),
                c.getNumeroParcelas(),
                c.calcularValorParcela(),
                c.getDataConcessao(),
                c.getBanco().getId(),
                c.getBanco().getNome()
        );
    }
}
