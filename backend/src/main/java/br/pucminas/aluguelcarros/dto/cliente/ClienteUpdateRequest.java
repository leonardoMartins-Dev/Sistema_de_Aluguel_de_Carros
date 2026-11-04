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
 * Atualização dos dados do cliente logado (US05).
 * Não tem login (o token JWT é amarrado a ele) e a senha é opcional: vazia = mantém a atual.
 */
public record ClienteUpdateRequest(
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

        @NotBlank(message = "Informe o CPF.")
        @CPF(message = "CPF inválido.")
        String cpf,

        @NotBlank(message = "Informe a profissão.")
        @Size(max = 80)
        String profissao,

        @NotNull(message = "Informe o endereço.")
        @Valid
        EnderecoDTO endereco,

        @Size(max = Cliente.MAX_RENDIMENTOS, message = "Informe no máximo 3 rendimentos.")
        List<@Valid RendimentoDTO> rendimentos
) {
    public ClienteUpdateRequest {
        if (senha != null && senha.isBlank()) {
            senha = null;
        }
        if (cpf != null) {
            cpf = cpf.replaceAll("\\D", "");
        }
        if (rendimentos == null) {
            rendimentos = List.of();
        }
    }

    public void copiarPara(Cliente cliente) {
        cliente.setEmail(email);
        cliente.setNome(nome);
        cliente.setRg(rg);
        cliente.setCpf(cpf);
        cliente.setProfissao(profissao);
        cliente.setEndereco(endereco.toEntity());

        List<Rendimento> novos = rendimentos.stream().map(RendimentoDTO::toEntity).toList();
        cliente.substituirRendimentos(novos);
    }
}
