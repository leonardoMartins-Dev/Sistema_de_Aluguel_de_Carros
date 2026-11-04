package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Banco: único agente que pode conceder contrato de crédito (RN19).
 */
@Entity
@Table(name = "banco")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banco extends Agente {

    @Column(name = "codigo_bancario", nullable = false, unique = true, length = 10)
    private String codigoBancario;

    public Banco(String login, String senha, String email, String nome, String cnpj, String codigoBancario) {
        super(login, senha, email, Perfil.BANCO, nome, cnpj);
        this.codigoBancario = codigoBancario;
    }
}
