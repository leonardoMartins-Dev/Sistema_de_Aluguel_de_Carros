package br.pucminas.aluguelcarros.dto.auth;

import br.pucminas.aluguelcarros.model.enums.Perfil;

/**
 * Resposta do login (US02): o front guarda o token e usa o perfil para montar as telas.
 */
public record LoginResponse(
        String token,
        String tipo,
        Long id,
        String login,
        String nome,
        Perfil perfil
) {
    public LoginResponse(String token, Long id, String login, String nome, Perfil perfil) {
        this(token, "Bearer", id, login, nome, perfil);
    }
}
