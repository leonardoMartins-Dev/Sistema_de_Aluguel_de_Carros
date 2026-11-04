package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.model.enums.Perfil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cliente (contratante do aluguel).
 * RN04: RG, CPF, nome, endereço e profissão são obrigatórios.
 * RN06: CPF e RG são únicos.
 */
@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cliente extends Usuario {

    public static final int MAX_RENDIMENTOS = 3;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String rg;

    // Guardado só com os 11 dígitos, sem pontuação
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 80)
    private String profissao;

    @Embedded
    private Endereco endereco;

    // BatchSize: ao listar pedidos de vários clientes, carrega os rendimentos de todos numa consulta só
    @Setter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cliente_id", nullable = false)
    @BatchSize(size = 50)
    private List<Rendimento> rendimentos = new ArrayList<>();

    public Cliente(String login, String senha, String email,
                   String nome, String rg, String cpf, String profissao, Endereco endereco) {
        super(login, senha, email, Perfil.CLIENTE);
        this.nome = nome;
        this.rg = rg;
        this.cpf = cpf;
        this.profissao = profissao;
        this.endereco = endereco;
    }

    /** Somente leitura: para alterar use adicionar/remover/substituirRendimentos. */
    public List<Rendimento> getRendimentos() {
        return Collections.unmodifiableList(rendimentos);
    }

    /** RN05: no máximo 3 rendimentos, cada um com valor maior que zero. */
    public void adicionarRendimento(Rendimento rendimento) {
        if (rendimentos.size() >= MAX_RENDIMENTOS) {
            throw new BusinessException("O cliente pode informar no máximo " + MAX_RENDIMENTOS + " rendimentos.");
        }
        if (rendimento.getValor() == null || rendimento.getValor().signum() <= 0) {
            throw new BusinessException("O valor do rendimento deve ser maior que zero.");
        }
        rendimentos.add(rendimento);
    }

    public void removerRendimento(Rendimento rendimento) {
        rendimentos.remove(rendimento);
    }

    /** Troca todos os rendimentos de uma vez (usado na atualização do cadastro). */
    public void substituirRendimentos(List<Rendimento> novos) {
        if (novos.size() > MAX_RENDIMENTOS) {
            throw new BusinessException("O cliente pode informar no máximo " + MAX_RENDIMENTOS + " rendimentos.");
        }
        rendimentos.clear();
        novos.forEach(this::adicionarRendimento);
    }

    public BigDecimal getRendaTotal() {
        return rendimentos.stream()
                .map(Rendimento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
