package br.pucminas.aluguelcarros.dto.automovel;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Usuario;

public record AutomovelResponse(
        Long id,
        String matricula,
        Integer ano,
        String marca,
        String modelo,
        String placa,
        Long proprietarioId,
        String proprietarioNome
) {
    public static AutomovelResponse from(Automovel a) {
        Usuario dono = a.getProprietario();
        return new AutomovelResponse(
                a.getId(),
                a.getMatricula(),
                a.getAno(),
                a.getMarca(),
                a.getModelo(),
                a.getPlaca(),
                dono != null ? dono.getId() : null,
                dono != null ? dono.getNome() : null
        );
    }
}
