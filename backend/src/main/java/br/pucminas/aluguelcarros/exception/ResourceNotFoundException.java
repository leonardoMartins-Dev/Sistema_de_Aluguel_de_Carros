package br.pucminas.aluguelcarros.exception;

/**
 * Recurso não encontrado (ou que o usuário logado não pode ver).
 * O GlobalExceptionHandler devolve 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " não encontrado(a) com id " + id + ".");
    }
}
