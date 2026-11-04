package br.pucminas.aluguelcarros.dto.contrato;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** Concessão de crédito pelo banco (US17 / RN19). */
public record ContratoCreditoRequest(
        @NotNull(message = "Informe o valor do crédito.")
        @Positive(message = "O valor do crédito deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal valor,

        // Percentual ao mês: 1.99 = 1,99% a.m.
        @NotNull(message = "Informe a taxa de juros mensal.")
        @PositiveOrZero(message = "A taxa de juros não pode ser negativa.")
        @DecimalMax(value = "100", message = "A taxa de juros deve ser no máximo 100% a.m.")
        @Digits(integer = 3, fraction = 4)
        BigDecimal taxaJurosMensal,

        @NotNull(message = "Informe o número de parcelas.")
        @Min(value = 1, message = "O número de parcelas deve ser pelo menos 1.")
        @Max(value = 120, message = "O número de parcelas deve ser no máximo 120.")
        Integer numeroParcelas
) {
}
