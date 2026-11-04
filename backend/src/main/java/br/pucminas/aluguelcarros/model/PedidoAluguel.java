package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pedido de aluguel feito pelo cliente.
 * Não tem setters: o status só muda pelos métodos de transição, que conferem o status atual
 * (ciclo: EM_ANALISE → APROVADO/REPROVADO → CONTRATADO, ou CANCELADO antes de contratar).
 */
@Entity
@Table(name = "pedido_aluguel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoAluguel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status;

    @Column(length = 500)
    private String parecer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "automovel_id", nullable = false)
    private Automovel automovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_avaliador_id")
    private Agente agenteAvaliador;

    /** RN08: todo pedido novo nasce EM_ANALISE. */
    public PedidoAluguel(Cliente cliente, Automovel automovel, LocalDate dataInicio, LocalDate dataFim) {
        validarPeriodo(dataInicio, dataFim);
        this.cliente = cliente;
        this.automovel = automovel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = StatusPedido.EM_ANALISE;
        this.dataCriacao = LocalDateTime.now();
    }

    /** RN10 / RN14: só modifica enquanto estiver EM_ANALISE. */
    public void modificar(LocalDate dataInicio, LocalDate dataFim, Automovel automovel) {
        if (!isEditavel()) {
            throw new BusinessException("Só é possível modificar pedidos em análise. Status atual: " + status + ".");
        }
        validarPeriodo(dataInicio, dataFim);
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.automovel = automovel;
    }

    /** RN11: cancela se estiver EM_ANALISE ou APROVADO. */
    public void cancelar() {
        if (!isCancelavel()) {
            throw new BusinessException("Só é possível cancelar pedidos em análise ou aprovados. Status atual: " + status + ".");
        }
        this.status = StatusPedido.CANCELADO;
    }

    /** RN15: parecer positivo → APROVADO. */
    public void aprovar(Agente agente, String parecer) {
        registrarAvaliacao(agente, parecer);
        this.status = StatusPedido.APROVADO;
    }

    /** RN15: parecer negativo → REPROVADO. */
    public void reprovar(Agente agente, String parecer) {
        registrarAvaliacao(agente, parecer);
        this.status = StatusPedido.REPROVADO;
    }

    /** RN16: só um pedido APROVADO vira CONTRATADO. */
    public void marcarComoContratado() {
        if (status != StatusPedido.APROVADO) {
            throw new BusinessException("Só é possível contratar pedidos aprovados. Status atual: " + status + ".");
        }
        this.status = StatusPedido.CONTRATADO;
    }

    public boolean isEditavel() {
        return status == StatusPedido.EM_ANALISE;
    }

    public boolean isCancelavel() {
        return status == StatusPedido.EM_ANALISE || status == StatusPedido.APROVADO;
    }

    private void registrarAvaliacao(Agente agente, String parecer) {
        if (status != StatusPedido.EM_ANALISE) {
            throw new BusinessException("Só é possível avaliar pedidos em análise. Status atual: " + status + ".");
        }
        this.agenteAvaliador = agente;
        this.parecer = parecer;
    }

    /** RN13: início não pode estar no passado e o fim tem que ser depois do início. */
    private static void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new BusinessException("Informe a data de início e a data de fim.");
        }
        if (dataInicio.isBefore(LocalDate.now())) {
            throw new BusinessException("A data de início não pode estar no passado.");
        }
        if (!dataFim.isAfter(dataInicio)) {
            throw new BusinessException("A data de fim deve ser posterior à data de início.");
        }
    }
}
