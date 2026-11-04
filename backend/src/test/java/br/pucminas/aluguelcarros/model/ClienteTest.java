package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private Cliente novoCliente() {
        Endereco endereco = new Endereco("Rua A", "10", null, "Centro", "Belo Horizonte", "MG", "30110-000");
        return new Cliente("maria", "hash", "maria@exemplo.com", "Maria", "MG123", "52998224725", "Engenheira", endereco);
    }

    private Rendimento rendimento(String valor) {
        return new Rendimento("Empresa", new BigDecimal(valor));
    }

    @Test
    void aceitaAteTresRendimentosESomaARenda() {
        Cliente cliente = novoCliente();
        cliente.adicionarRendimento(rendimento("1000"));
        cliente.adicionarRendimento(rendimento("2000.50"));
        cliente.adicionarRendimento(rendimento("1"));

        assertThat(cliente.getRendimentos()).hasSize(3);
        assertThat(cliente.getRendaTotal()).isEqualByComparingTo("3001.50");
    }

    @Test
    void recusaQuartoRendimento() { // RN05
        Cliente cliente = novoCliente();
        cliente.substituirRendimentos(List.of(rendimento("1"), rendimento("2"), rendimento("3")));

        assertThatThrownBy(() -> cliente.adicionarRendimento(rendimento("4")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> cliente.substituirRendimentos(
                List.of(rendimento("1"), rendimento("2"), rendimento("3"), rendimento("4"))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void recusaRendimentoZeroOuNegativo() { // RN05
        Cliente cliente = novoCliente();

        assertThatThrownBy(() -> cliente.adicionarRendimento(rendimento("0")))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> cliente.adicionarRendimento(rendimento("-10")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listaDeRendimentosNaoPodeSerAlteradaPorFora() {
        Cliente cliente = novoCliente();

        assertThatThrownBy(() -> cliente.getRendimentos().add(rendimento("1")))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
