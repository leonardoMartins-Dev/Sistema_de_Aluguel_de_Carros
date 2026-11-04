package br.pucminas.aluguelcarros.dto.automovel;

import br.pucminas.aluguelcarros.model.Automovel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Cadastro/edição de automóvel pelos agentes (US12). */
public record AutomovelRequest(
        @NotBlank(message = "Informe a matrícula.")
        @Size(max = 20)
        String matricula,

        @NotNull(message = "Informe o ano.")
        @Min(value = 1900, message = "Ano inválido.")
        @Max(value = 2100, message = "Ano inválido.")
        Integer ano,

        @NotBlank(message = "Informe a marca.")
        @Size(max = 50)
        String marca,

        @NotBlank(message = "Informe o modelo.")
        @Size(max = 80)
        String modelo,

        // Aceita o padrão antigo (ABC1234) e o Mercosul (ABC1D23)
        @NotBlank(message = "Informe a placa.")
        @Pattern(regexp = "[A-Z]{3}\\d[A-Z0-9]\\d{2}", message = "Placa inválida.")
        String placa
) {
    public AutomovelRequest {
        // Normaliza "abc-1234" para "ABC1234" antes de validar e checar unicidade
        if (placa != null) {
            placa = placa.replace("-", "").trim().toUpperCase();
        }
    }

    public Automovel toEntity() {
        return new Automovel(matricula, ano, marca, modelo, placa);
    }

    public void copiarPara(Automovel automovel) {
        automovel.setMatricula(matricula);
        automovel.setAno(ano);
        automovel.setMarca(marca);
        automovel.setModelo(modelo);
        automovel.setPlaca(placa);
    }
}
