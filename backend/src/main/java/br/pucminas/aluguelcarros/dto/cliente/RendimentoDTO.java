package br.pucminas.aluguelcarros.dto.cliente;

import br.pucminas.aluguelcarros.model.Rendimento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RendimentoDTO(
        @NotBlank(message = "Informe a entidade empregadora.")
        @Size(max = 150)
        String entidadeEmpregadora,

        // RN05: valor maior que zero
        @NotNull(message = "Informe o valor do rendimento.")
        @Positive(message = "O valor do rendimento deve ser maior que zero.")
        @Digits(integer = 10, fraction = 2)
        BigDecimal valor
) {
    public static RendimentoDTO from(Rendimento r) {
        return new RendimentoDTO(r.getEntidadeEmpregadora(), r.getValor());
    }

    public Rendimento toEntity() {
        return new Rendimento(entidadeEmpregadora, valor);
    }
}
