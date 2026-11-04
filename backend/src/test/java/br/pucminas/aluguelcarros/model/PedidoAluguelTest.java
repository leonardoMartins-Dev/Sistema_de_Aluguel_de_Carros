package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.model.enums.StatusPedido;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoAluguelTest {

    private final LocalDate hoje = LocalDate.now();
    private final Cliente cliente = new Cliente("c", "h", "c@x.com", "Cliente", "rg", "52998224725", "Prof",
            new Endereco("Rua", "1", null, "Centro", "BH", "MG", "30110-000"));
    private final Empresa empresa = new Empresa("e", "h", "e@x.com", "Empresa", "1");
    private final Automovel carro = new Automovel("m1", 2022, "Fiat", "Argo", "ABC1D23");

    private PedidoAluguel novoPedido() {
        return new PedidoAluguel(cliente, carro, hoje.plusDays(1), hoje.plusDays(5));
    }

    @Test
    void nasceEmAnalise() { // RN08
        PedidoAluguel pedido = novoPedido();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.EM_ANALISE);
        assertThat(pedido.isEditavel()).isTrue();
        assertThat(pedido.isCancelavel()).isTrue();
        assertThat(pedido.getDataCriacao()).isNotNull();
    }

    @Test
    void validaPeriodo() { // RN13
        assertThatThrownBy(() -> new PedidoAluguel(cliente, carro, hoje.minusDays(1), hoje.plusDays(2)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("passado");
        assertThatThrownBy(() -> new PedidoAluguel(cliente, carro, hoje, hoje))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void cicloCompletoAteContratado() { // RN15, RN16
        PedidoAluguel pedido = novoPedido();
        pedido.modificar(hoje.plusDays(2), hoje.plusDays(6), carro);

        pedido.aprovar(empresa, "Renda compatível");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.APROVADO);
        assertThat(pedido.getAgenteAvaliador()).isSameAs(empresa);
        assertThat(pedido.getParecer()).isEqualTo("Renda compatível");

        pedido.marcarComoContratado();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONTRATADO);
    }

    @Test
    void soModificaEmAnalise() { // RN10, RN14
        PedidoAluguel pedido = novoPedido();
        pedido.aprovar(empresa, "ok");

        assertThatThrownBy(() -> pedido.modificar(hoje.plusDays(2), hoje.plusDays(6), carro))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void soAvaliaEmAnalise() { // RN14
        PedidoAluguel pedido = novoPedido();
        pedido.reprovar(empresa, "Renda insuficiente");

        assertThatThrownBy(() -> pedido.aprovar(empresa, "mudei de ideia"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void soContrataAprovado() { // RN16
        assertThatThrownBy(() -> novoPedido().marcarComoContratado())
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelaEmAnaliseOuAprovado() { // RN11
        PedidoAluguel emAnalise = novoPedido();
        emAnalise.cancelar();
        assertThat(emAnalise.getStatus()).isEqualTo(StatusPedido.CANCELADO);

        PedidoAluguel aprovado = novoPedido();
        aprovado.aprovar(empresa, "ok");
        aprovado.cancelar();
        assertThat(aprovado.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void naoCancelaContratadoReprovadoOuCancelado() { // RN11, RN12
        PedidoAluguel contratado = novoPedido();
        contratado.aprovar(empresa, "ok");
        contratado.marcarComoContratado();
        assertThatThrownBy(contratado::cancelar).isInstanceOf(BusinessException.class);

        PedidoAluguel reprovado = novoPedido();
        reprovado.reprovar(empresa, "não");
        assertThatThrownBy(reprovado::cancelar).isInstanceOf(BusinessException.class);

        PedidoAluguel cancelado = novoPedido();
        cancelado.cancelar();
        assertThatThrownBy(cancelado::cancelar).isInstanceOf(BusinessException.class);
    }
}
