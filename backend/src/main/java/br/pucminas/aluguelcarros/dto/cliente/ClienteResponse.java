package br.pucminas.aluguelcarros.dto.cliente;

import br.pucminas.aluguelcarros.model.Cliente;

import java.math.BigDecimal;
import java.util.List;

/** Dados do cliente devolvidos pela API. Nunca inclui a senha (RN02). */
public record ClienteResponse(
        Long id,
        String login,
        String email,
        String nome,
        String rg,
        String cpf,
        String profissao,
        EnderecoDTO endereco,
        List<RendimentoDTO> rendimentos,
        BigDecimal rendaTotal
) {
    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(
                c.getId(),
                c.getLogin(),
                c.getEmail(),
                c.getNome(),
                c.getRg(),
                c.getCpf(),
                c.getProfissao(),
                EnderecoDTO.from(c.getEndereco()),
                c.getRendimentos().stream().map(RendimentoDTO::from).toList(),
                c.getRendaTotal()
        );
    }
}
