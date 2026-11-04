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
 * Agente que avalia pedidos e executa contratos (RN03, RN14).
 * Pode ser uma Empresa ou um Banco.
 */
@Entity
@Table(name = "agente")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Agente extends Usuario {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    protected Agente(String login, String senha, String email, Perfil perfil, String nome, String cnpj) {
        super(login, senha, email, perfil);
        this.nome = nome;
        this.cnpj = cnpj;
    }
}
