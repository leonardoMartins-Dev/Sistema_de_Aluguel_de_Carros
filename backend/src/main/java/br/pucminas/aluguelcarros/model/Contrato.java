package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.model.enums.Perfil;
import br.pucminas.aluguelcarros.model.enums.TipoContrato;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato gerado a partir de um pedido aprovado.
 * RN17: cada pedido gera no máximo um contrato (pedido_id é unique).
 * RN18: o tipo define quem fica como proprietário do automóvel.
 */
@Entity
@Table(name = "contrato")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoContrato tipo;

    @Column(name = "data_assinatura", nullable = false)
    private LocalDate dataAssinatura;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private PedidoAluguel pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agente_responsavel_id", nullable = false)
    private Agente agenteResponsavel;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "contrato_credito_id", unique = true)
    private ContratoCredito contratoCredito;

    public Contrato(PedidoAluguel pedido, Agente agenteResponsavel, TipoContrato tipo, BigDecimal valorTotal) {
        this.pedido = pedido;
        this.agenteResponsavel = agenteResponsavel;
        this.tipo = tipo;
        this.valorTotal = valorTotal;
        this.dataAssinatura = LocalDate.now();
    }

    /** RN19: o aluguel pode ter um contrato de crédito (só um). */
    public void associarCredito(ContratoCredito credito) {
        if (possuiCredito()) {
            throw new BusinessException("Este contrato já possui um contrato de crédito.");
        }
        this.contratoCredito = credito;
    }

    public boolean possuiCredito() {
        return contratoCredito != null;
    }

    /**
     * RN18: devolve quem deve ficar registrado como proprietário do automóvel.
     * Usa o perfil em vez de instanceof porque o agente pode vir como proxy LAZY do Hibernate.
     */
    public Usuario definirProprietario() {
        return switch (tipo) {
            case PROPRIEDADE_CLIENTE -> pedido.getCliente();
            case PROPRIEDADE_EMPRESA -> {
                if (agenteResponsavel.getPerfil() != Perfil.EMPRESA) {
                    throw new BusinessException("Contrato com propriedade da empresa deve ser executado por uma empresa.");
                }
                yield agenteResponsavel;
            }
            case PROPRIEDADE_BANCO -> {
                if (possuiCredito()) {
                    yield contratoCredito.getBanco();
                }
                if (agenteResponsavel.getPerfil() != Perfil.BANCO) {
                    throw new BusinessException("Contrato com propriedade do banco deve ser executado por um banco ou ter crédito concedido por um banco.");
                }
                yield agenteResponsavel;
            }
        };
    }
}
