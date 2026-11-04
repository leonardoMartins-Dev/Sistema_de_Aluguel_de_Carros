package br.pucminas.aluguelcarros.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato padrão de erro devolvido pela API, para o front-end mostrar mensagens.
 * "campos" só vem preenchido em erro de validação (campo → mensagem).
 */
public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
}
