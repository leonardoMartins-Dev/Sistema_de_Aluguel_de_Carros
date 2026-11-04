package br.pucminas.aluguelcarros.dto.pedido;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Introduzir (US08) ou modificar (US10, US14) um pedido.
 * A regra "fim depois do início" (RN13) é conferida em PedidoAluguel.
 */
public record PedidoRequest(
        @NotNull(message = "Informe o automóvel.")
        Long automovelId,

        @NotNull(message = "Informe a data de início.")
        @FutureOrPresent(message = "A data de início não pode estar no passado.")
        LocalDate dataInicio,

        @NotNull(message = "Informe a data de fim.")
        LocalDate dataFim
) {
}
