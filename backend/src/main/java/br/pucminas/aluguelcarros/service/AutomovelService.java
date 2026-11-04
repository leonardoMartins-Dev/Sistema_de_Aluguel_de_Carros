package br.pucminas.aluguelcarros.service;

import br.pucminas.aluguelcarros.dto.automovel.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.automovel.AutomovelResponse;
import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.exception.ResourceNotFoundException;
import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.PedidoAluguelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Consulta (US07) e manutenção de automóveis pelos agentes (US12). */
@Service
@RequiredArgsConstructor
public class AutomovelService {

    private final AutomovelRepository automovelRepository;
    private final PedidoAluguelRepository pedidoRepository;

    @Transactional(readOnly = true)
    public List<AutomovelResponse> listar() {
        return automovelRepository.findAllByOrderByMarcaAscModeloAsc().stream()
                .map(AutomovelResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AutomovelResponse buscar(Long id) {
        return AutomovelResponse.from(buscarEntidade(id));
    }

    @Transactional
    public AutomovelResponse criar(AutomovelRequest request) {
        // RN20: placa e matrícula únicas
        if (automovelRepository.existsByPlaca(request.placa())) {
            throw new BusinessException("Já existe um automóvel com esta placa.");
        }
        if (automovelRepository.existsByMatricula(request.matricula())) {
            throw new BusinessException("Já existe um automóvel com esta matrícula.");
        }
        return AutomovelResponse.from(automovelRepository.save(request.toEntity()));
    }

    @Transactional
    public AutomovelResponse atualizar(Long id, AutomovelRequest request) {
        Automovel automovel = buscarEntidade(id);

        if (automovelRepository.existsByPlacaAndIdNot(request.placa(), id)) {
            throw new BusinessException("Já existe um automóvel com esta placa.");
        }
        if (automovelRepository.existsByMatriculaAndIdNot(request.matricula(), id)) {
            throw new BusinessException("Já existe um automóvel com esta matrícula.");
        }

        request.copiarPara(automovel);
        return AutomovelResponse.from(automovel);
    }

    @Transactional
    public void excluir(Long id) {
        Automovel automovel = buscarEntidade(id);
        if (pedidoRepository.existsByAutomovelId(id)) {
            throw new BusinessException("Este automóvel tem pedidos registrados e não pode ser excluído.");
        }
        automovelRepository.delete(automovel);
    }

    private Automovel buscarEntidade(Long id) {
        return automovelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automóvel", id));
    }
}
