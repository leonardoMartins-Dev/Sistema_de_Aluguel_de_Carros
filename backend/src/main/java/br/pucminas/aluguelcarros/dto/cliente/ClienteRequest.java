package br.pucminas.aluguelcarros.dto.cliente;

import br.pucminas.aluguelcarros.model.Cliente;
import br.pucminas.aluguelcarros.model.Rendimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.util.List;

/**
 * Cadastro do cliente (US01) e atualização dos dados (US05).
 */
public record ClienteRequest(
        @NotBlank(message = "Informe o login.")
        @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres.")
        String login,

        @NotBlank(message = "Informe a senha.")
        @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres.")
        String senha,

        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "E-mail inválido.")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Informe o nome.")
        @Size(max = 100)
        String nome,

        @NotBlank(message = "Informe o RG.")
        @Size(max = 20)
        String rg,

        // RN06: CPF válido (confere os dígitos verificadores)
        @NotBlank(message = "Informe o CPF.")
        @CPF(message = "CPF inválido.")
        String cpf,

        @NotBlank(message = "Informe a profissão.")
        @Size(max = 80)
        String profissao,

        @NotNull(message = "Informe o endereço.")
        @Valid
        EnderecoDTO endereco,

        // RN05: no máximo 3 rendimentos
        @Size(max = Cliente.MAX_RENDIMENTOS, message = "Informe no máximo 3 rendimentos.")
        List<@Valid RendimentoDTO> rendimentos
) {
    public ClienteRequest {
        // Guarda o CPF só com dígitos, para a checagem de unicidade não depender da máscara
        if (cpf != null) {
            cpf = cpf.replaceAll("\\D", "");
        }
        if (rendimentos == null) {
            rendimentos = List.of();
        }
    }

    public List<Rendimento> rendimentosComoEntidade() {
        return rendimentos.stream().map(RendimentoDTO::toEntity).toList();
    }

    public Cliente toEntity(String senhaCriptografada) {
        Cliente cliente = new Cliente(login, senhaCriptografada, email, nome, rg, cpf, profissao, endereco.toEntity());
        cliente.substituirRendimentos(rendimentosComoEntidade());
        return cliente;
    }
}
