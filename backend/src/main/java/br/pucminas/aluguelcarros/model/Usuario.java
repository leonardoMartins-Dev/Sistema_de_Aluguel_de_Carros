package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.model.enums.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base de todos os usuários do sistema (Cliente, Empresa e Banco).
 * JOINED: cria a tabela "usuario" com os dados de acesso e uma tabela por subclasse.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RN02: login único
    @Column(nullable = false, unique = true, length = 50)
    private String login;

    // RN02: guarda só o hash BCrypt, nunca a senha pura
    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false, length = 150)
    private String email;

    // RN03: definido pela subclasse e não muda depois
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    protected Usuario(String login, String senha, String email, Perfil perfil) {
        this.login = login;
        this.senha = senha;
        this.email = email;
        this.perfil = perfil;
    }

    /** Nome de exibição: Cliente e Agente têm o atributo "nome". */
    public abstract String getNome();
}
