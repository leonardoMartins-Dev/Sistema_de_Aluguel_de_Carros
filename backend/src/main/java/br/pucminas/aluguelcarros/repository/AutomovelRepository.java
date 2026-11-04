package br.pucminas.aluguelcarros.repository;

import br.pucminas.aluguelcarros.model.Automovel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    // RN20: matrícula e placa únicas
    boolean existsByPlaca(String placa);

    boolean existsByMatricula(String matricula);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    @EntityGraph(attributePaths = "proprietario")
    List<Automovel> findAllByOrderByMarcaAscModeloAsc();
}
