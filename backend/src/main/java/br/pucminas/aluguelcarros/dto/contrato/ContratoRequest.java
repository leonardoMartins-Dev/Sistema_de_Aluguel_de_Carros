package br.pucminas.aluguelcarros.dto.contrato;

import br.pucminas.aluguelcarros.model.enums.TipoContrato;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Execução do contrato de um pedido aprovado (US16). */
public record ContratoRequest(
        @NotNull(message = "Informe o pedido.")
        Long pedidoId,

        // RN18: define quem fica como proprietário do automóvel
        @NotNull(message = "Informe o tipo de contrato.")
        TipoContrato tipo,

        @NotNull(message = "Informe o valor total.")
        @Positive(message = "O valor total deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal valorTotal
) {
}
