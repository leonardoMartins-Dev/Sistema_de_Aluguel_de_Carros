package br.pucminas.aluguelcarros.exception;

/**
 * Violação de regra de negócio (ex.: mais de 3 rendimentos, pedido fora de EM_ANALISE).
 * O GlobalExceptionHandler devolve 422 (Unprocessable Entity).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String mensagem) {
        super(mensagem);
    }
}
