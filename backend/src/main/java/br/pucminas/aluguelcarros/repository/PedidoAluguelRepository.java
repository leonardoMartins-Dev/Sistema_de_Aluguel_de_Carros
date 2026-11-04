package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.PedidoAluguel;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PedidoAluguelRepository extends JpaRepository<PedidoAluguel, Long> {

    // Já traz cliente, automóvel e agente juntos para montar o DTO sem N+1 (open-in-view=false).
    // Os rendimentos do cliente ficam de fora do join (duplicariam a lista); vêm em lote pelo @BatchSize em Cliente.
    @EntityGraph(attributePaths = {"cliente", "automovel", "automovel.proprietario", "agenteAvaliador"})
    List<PedidoAluguel> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    @EntityGraph(attributePaths = {"cliente", "automovel", "automovel.proprietario", "agenteAvaliador"})
    List<PedidoAluguel> findByStatusOrderByDataCriacaoAsc(StatusPedido status);

    @EntityGraph(attributePaths = {"cliente", "automovel", "automovel.proprietario", "agenteAvaliador"})
    List<PedidoAluguel> findAllByOrderByDataCriacaoDesc();

    // RN22: cliente com pedido CONTRATADO não pode excluir a conta
    boolean existsByClienteIdAndStatus(Long clienteId, StatusPedido status);

    // Usado ao excluir a conta do cliente (os pedidos restantes não têm contrato)
    void deleteByClienteId(Long clienteId);

    // Automóvel com pedido não pode ser excluído
    boolean existsByAutomovelId(Long automovelId);

    // RN21: o mesmo carro não pode ter dois pedidos (nos status informados) com períodos sobrepostos
    @Query("""
            select count(p) > 0 from PedidoAluguel p
            where p.automovel.id = :automovelId
              and p.status in :status
              and p.dataInicio <= :dataFim
              and p.dataFim >= :dataInicio
            """)
    boolean existsConflitoDePeriodo(@Param("automovelId") Long automovelId,
                                    @Param("dataInicio") LocalDate dataInicio,
                                    @Param("dataFim") LocalDate dataFim,
                                    @Param("status") Collection<StatusPedido> status);
}
