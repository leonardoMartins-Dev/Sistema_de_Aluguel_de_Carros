package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Banco;
import org.springframework.data.jpa.repository.JpaRepository;

// RN19: o crédito precisa de um Banco (não de um Agente qualquer)
public interface BancoRepository extends JpaRepository<Banco, Long> {
}
