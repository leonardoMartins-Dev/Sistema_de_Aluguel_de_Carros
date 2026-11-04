package br.pucminas.aluguelcarros.dto.pedido;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Avaliação financeira do agente (US15): aprovado=true → APROVADO, false → REPROVADO. */
public record AvaliacaoRequest(
        @NotNull(message = "Informe se o parecer é positivo ou negativo.")
        Boolean aprovado,

        @NotBlank(message = "Informe o parecer.")
        @Size(max = 500)
        String parecer
) {
}
