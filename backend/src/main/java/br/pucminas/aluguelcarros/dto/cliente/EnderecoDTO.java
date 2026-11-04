package br.pucminas.aluguelcarros.dto.cliente;

import br.pucminas.aluguelcarros.model.Endereco;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoDTO(
        @NotBlank(message = "Informe o logradouro.")
        @Size(max = 150)
        String logradouro,

        @NotBlank(message = "Informe o número.")
        @Size(max = 10)
        String numero,

        @Size(max = 100)
        String complemento,

        @NotBlank(message = "Informe o bairro.")
        @Size(max = 80)
        String bairro,

        @NotBlank(message = "Informe a cidade.")
        @Size(max = 80)
        String cidade,

        @NotBlank(message = "Informe a UF.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras.")
        String uf,

        @NotBlank(message = "Informe o CEP.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido.")
        String cep
) {
    public static EnderecoDTO from(Endereco e) {
        if (e == null) {
            return null;
        }
        return new EnderecoDTO(e.getLogradouro(), e.getNumero(), e.getComplemento(),
                e.getBairro(), e.getCidade(), e.getUf(), e.getCep());
    }

    public Endereco toEntity() {
        return new Endereco(logradouro, numero, complemento, bairro, cidade, uf.toUpperCase(), cep);
    }
}
