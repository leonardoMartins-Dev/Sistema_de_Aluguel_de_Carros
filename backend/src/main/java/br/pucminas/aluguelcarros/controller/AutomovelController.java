package br.pucminas.aluguelcarros.controller;

import br.pucminas.aluguelcarros.dto.automovel.AutomovelRequest;
import br.pucminas.aluguelcarros.dto.automovel.AutomovelResponse;
import br.pucminas.aluguelcarros.service.AutomovelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET: qualquer usuário logado. POST/PUT/DELETE: EMPRESA ou BANCO (SecurityConfig). */
@RestController
@RequestMapping("/api/automoveis")
@RequiredArgsConstructor
public class AutomovelController {

    private final AutomovelService automovelService;

    // US07
    @GetMapping
    public List<AutomovelResponse> listar() {
        return automovelService.listar();
    }

    @GetMapping("/{id}")
    public AutomovelResponse buscar(@PathVariable Long id) {
        return automovelService.buscar(id);
    }

    // US12
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomovelResponse criar(@RequestBody @Valid AutomovelRequest request) {
        return automovelService.criar(request);
    }

    @PutMapping("/{id}")
    public AutomovelResponse atualizar(@PathVariable Long id, @RequestBody @Valid AutomovelRequest request) {
        return automovelService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        automovelService.excluir(id);
    }
}
