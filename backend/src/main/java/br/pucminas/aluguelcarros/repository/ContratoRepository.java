package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Contrato;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    // RN17: no máximo um contrato por pedido
    boolean existsByPedidoId(Long pedidoId);

    Optional<Contrato> findByPedidoId(Long pedidoId);

    @EntityGraph(attributePaths = {"agenteResponsavel", "contratoCredito", "contratoCredito.banco",
            "pedido", "pedido.cliente", "pedido.automovel", "pedido.automovel.proprietario"})
    List<Contrato> findAllByOrderByIdDesc();
}
