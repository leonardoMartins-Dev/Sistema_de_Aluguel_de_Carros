package br.pucminas.aluguelcarros.model;

import br.pucminas.aluguelcarros.exception.BusinessException;
import br.pucminas.aluguelcarros.model.enums.TipoContrato;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContratoTest {

    private final Cliente cliente = new Cliente("c", "h", "c@x.com", "Cliente", "rg", "52998224725", "Prof",
            new Endereco("Rua", "1", null, "Centro", "BH", "MG", "30110-000"));
    private final Empresa empresa = new Empresa("e", "h", "e@x.com", "Empresa", "1");
    private final Banco banco = new Banco("b", "h", "b@x.com", "Banco", "2", "999");
    private final PedidoAluguel pedido = new PedidoAluguel(cliente,
            new Automovel("m1", 2022, "Fiat", "Argo", "ABC1D23"),
            LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

    private Contrato contrato(Agente agente, TipoContrato tipo) {
        return new Contrato(pedido, agente, tipo, new BigDecimal("1500"));
    }

    private ContratoCredito credito(String valor, String taxa, int parcelas) {
        return new ContratoCredito(banco, new BigDecimal(valor), new BigDecimal(taxa), parcelas);
    }

    @Test
    void proprietarioConformeOTipo() { // RN18
        assertThat(contrato(empresa, TipoContrato.PROPRIEDADE_CLIENTE).definirProprietario()).isSameAs(cliente);
        assertThat(contrato(empresa, TipoContrato.PROPRIEDADE_EMPRESA).definirProprietario()).isSameAs(empresa);
        assertThat(contrato(banco, TipoContrato.PROPRIEDADE_BANCO).definirProprietario()).isSameAs(banco);
    }

    @Test
    void tipoIncompativelComOAgente() { // RN18
        assertThatThrownBy(() -> contrato(banco, TipoContrato.PROPRIEDADE_EMPRESA).definirProprietario())
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> contrato(empresa, TipoContrato.PROPRIEDADE_BANCO).definirProprietario())
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void propriedadeDoBancoViaCredito() { // RN18, RN19
        Contrato contrato = contrato(empresa, TipoContrato.PROPRIEDADE_BANCO);
        contrato.associarCredito(credito("10000", "1.5", 12));

        assertThat(contrato.possuiCredito()).isTrue();
        assertThat(contrato.definirProprietario()).isSameAs(banco);
    }

    @Test
    void apenasUmCreditoPorContrato() { // RN19
        Contrato contrato = contrato(banco, TipoContrato.PROPRIEDADE_CLIENTE);
        contrato.associarCredito(credito("1000", "1", 10));

        assertThatThrownBy(() -> contrato.associarCredito(credito("500", "1", 5)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void parcelaPelaTabelaPrice() {
        // 10.000 a 1,5% a.m. em 12x → 916,80
        assertThat(credito("10000", "1.5", 12).calcularValorParcela()).isEqualByComparingTo("916.80");
        // sem juros → divisão simples
        assertThat(credito("10000", "0", 12).calcularValorParcela()).isEqualByComparingTo("833.33");
    }
}
