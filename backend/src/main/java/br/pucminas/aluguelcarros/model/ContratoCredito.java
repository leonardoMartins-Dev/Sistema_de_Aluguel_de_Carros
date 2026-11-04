package br.pucminas.aluguelcarros.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Contrato de crédito associado ao aluguel.
 * RN19: só pode ser concedido por um Banco (por isso o atributo é do tipo Banco).
 */
@Entity
@Table(name = "contrato_credito")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    // Em percentual ao mês: 1.99 = 1,99% a.m.
    @Column(name = "taxa_juros_mensal", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxaJurosMensal;

    @Column(name = "numero_parcelas", nullable = false)
    private Integer numeroParcelas;

    @Column(name = "data_concessao", nullable = false)
    private LocalDate dataConcessao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    public ContratoCredito(Banco banco, BigDecimal valor, BigDecimal taxaJurosMensal, Integer numeroParcelas) {
        this.banco = banco;
        this.valor = valor;
        this.taxaJurosMensal = taxaJurosMensal;
        this.numeroParcelas = numeroParcelas;
        this.dataConcessao = LocalDate.now();
    }

    /** Parcela fixa pela Tabela Price: PMT = PV · i / (1 − (1 + i)^−n). */
    public BigDecimal calcularValorParcela() {
        if (taxaJurosMensal.signum() == 0) {
            return valor.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);
        }
        MathContext mc = MathContext.DECIMAL64;
        BigDecimal i = taxaJurosMensal.divide(BigDecimal.valueOf(100), mc);
        BigDecimal fator = BigDecimal.ONE.add(i).pow(numeroParcelas, mc);
        return valor.multiply(i).multiply(fator)
                .divide(fator.subtract(BigDecimal.ONE), mc)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
