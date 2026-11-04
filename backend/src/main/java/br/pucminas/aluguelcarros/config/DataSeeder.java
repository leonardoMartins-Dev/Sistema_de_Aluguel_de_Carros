package br.pucminas.aluguelcarros.config;

import br.pucminas.aluguelcarros.model.Automovel;
import br.pucminas.aluguelcarros.model.Banco;
import br.pucminas.aluguelcarros.model.Empresa;
import br.pucminas.aluguelcarros.repository.AutomovelRepository;
import br.pucminas.aluguelcarros.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * P1: os agentes (empresa e banco) não se cadastram pela tela, então são criados aqui ao subir a aplicação.
 * Também cria alguns automóveis de exemplo se a tabela estiver vazia.
 * Só insere o que ainda não existe, então pode rodar toda vez.
 */
@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AutomovelRepository automovelRepository;
    private final PasswordEncoder passwordEncoder;
    private final String senhaAgentes;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      AutomovelRepository automovelRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.senha-agentes}") String senhaAgentes) {
        this.usuarioRepository = usuarioRepository;
        this.automovelRepository = automovelRepository;
        this.passwordEncoder = passwordEncoder;
        this.senhaAgentes = senhaAgentes;
    }

    @Override
    public void run(String... args) {
        criarAgentes();
        criarAutomoveis();
    }

    private void criarAgentes() {
        if (!usuarioRepository.existsByLogin("empresa")) {
            usuarioRepository.save(new Empresa("empresa", passwordEncoder.encode(senhaAgentes),
                    "empresa@exemplo.com", "Locadora Exemplo LTDA", "12.345.678/0001-90"));
            log.info("Agente EMPRESA criado (login: empresa)");
        }
        if (!usuarioRepository.existsByLogin("banco")) {
            usuarioRepository.save(new Banco("banco", passwordEncoder.encode(senhaAgentes),
                    "banco@exemplo.com", "Banco Exemplo S.A.", "98.765.432/0001-10", "999"));
            log.info("Agente BANCO criado (login: banco)");
        }
    }

    private void criarAutomoveis() {
        if (automovelRepository.count() > 0) {
            return;
        }
        automovelRepository.saveAll(List.of(
                new Automovel("10000000001", 2022, "Fiat", "Argo", "ABC1D23"),
                new Automovel("10000000002", 2021, "Volkswagen", "Polo", "DEF4G56"),
                new Automovel("10000000003", 2023, "Chevrolet", "Onix", "GHI7J89"),
                new Automovel("10000000004", 2020, "Toyota", "Corolla", "JKL1234")
        ));
        log.info("Automóveis de exemplo criados");
    }
}
