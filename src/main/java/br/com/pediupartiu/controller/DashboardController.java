package br.com.pediupartiu.controller;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.pediupartiu.integracao.analise.AnaliseApiClient;
import br.com.pediupartiu.integracao.analise.ResumoAnaliseDTO;
import br.com.pediupartiu.service.PedidoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final PedidoService pedidoService;
    private final AnaliseApiClient analiseApiClient;

    public DashboardController(
            PedidoService pedidoService,
            AnaliseApiClient analiseApiClient) {

        this.pedidoService = pedidoService;
        this.analiseApiClient = analiseApiClient;
    }

    // carrega os indicadores e gráficos do dashboard
    @GetMapping("/dashboard")
    public String telaDashboard(
            Model model,
            @RequestParam(required = false) Integer dias) {

        try {

            // consulta os indicadores do microsserviço
            ResumoAnaliseDTO resumo =
                    analiseApiClient.consultarIndicadores(dias);

            if (resumo == null) {
                throw new IllegalStateException(
                        "resumo de análise indisponível");
            }

            model.addAttribute(
                    "pedidosRegistrados",
                    resumo.getPedidosRegistrados()
            );

            model.addAttribute(
                    "pedidosEntregues",
                    resumo.getPedidosEntregues()
            );

            model.addAttribute(
                    "pedidosCancelados",
                    resumo.getPedidosCancelados()
            );

            model.addAttribute(
                    "faturamentoDiaFormatado",
                    formatarMoeda(resumo.getFaturamento())
            );

            model.addAttribute(
                    "ticketMedioFormatado",
                    formatarMoeda(resumo.getTicketMedio())
            );

            model.addAttribute("diasSelecionados", dias);
            
         // carrega os produtos mais vendidos do microsserviço
            model.addAttribute(
                    "itensMaisPedidos",
                    prepararProdutosMaisVendidos(resumo)
            );
            
         // carrega as vendas diárias do microsserviço
            model.addAttribute(
                    "vendasPorDia",
                    prepararVendasPorDia(resumo)
            );

         // carrega as formas de pagamento do microsserviço
            model.addAttribute(
                    "formasPagamentoDashboard",
                    prepararFormasPagamento(resumo)
            );
        } catch (Exception e) {

            model.addAttribute(
                    "erro",
                    "Não foi possível carregar os indicadores do dashboard."
            );
        }

        return "dashboard/dashboard";
    }

    // formata valores monetários em reais
    private String formatarMoeda(double valor) {

        NumberFormat formatador =
                NumberFormat.getCurrencyInstance(
                        new Locale("pt", "BR")
                );

        return formatador.format(valor);
    }
    
 // prepara os produtos para o gráfico de barras
    private List<Map<String, Object>> prepararProdutosMaisVendidos(
            ResumoAnaliseDTO resumo) {

        List<Map<String, Object>> produtos = new ArrayList<>();

        if (resumo.getProdutosMaisVendidos() == null) {
            return produtos;
        }

        int maiorQuantidade = 0;

        // identifica a maior quantidade vendida
        for (Map<String, Object> produto : resumo.getProdutosMaisVendidos()) {

            int quantidade =
                    ((Number) produto.get("quantidadeVendida")).intValue();

            if (quantidade > maiorQuantidade) {
                maiorQuantidade = quantidade;
            }
        }

        // organiza os dados utilizados pelo html
        for (Map<String, Object> produto : resumo.getProdutosMaisVendidos()) {

            String nome = (String) produto.get("nomeProduto");

            int quantidade =
                    ((Number) produto.get("quantidadeVendida")).intValue();

            double percentual = 0;

            if (maiorQuantidade > 0) {
                percentual = (quantidade * 100.0) / maiorQuantidade;
            }

            Map<String, Object> item = new HashMap<>();

            item.put("nome", nome);
            item.put("quantidade", quantidade);
            item.put("percentual", percentual);

            produtos.add(item);
        }

        return produtos;
    }
    

    // prepara as vendas diárias para o gráfico vertical
    private List<Map<String, Object>> prepararVendasPorDia(
            ResumoAnaliseDTO resumo) {

        List<Map<String, Object>> vendas = new ArrayList<>();

        if (resumo.getVendasPorDia() == null) {
            return vendas;
        }

        double maiorFaturamento = 0;

        // identifica o maior faturamento diário
        for (Map<String, Object> venda : resumo.getVendasPorDia()) {

            double faturamento =
                    ((Number) venda.get("faturamento")).doubleValue();

            if (faturamento > maiorFaturamento) {
                maiorFaturamento = faturamento;
            }
        }

        DateTimeFormatter formatadorData =
                DateTimeFormatter.ofPattern("dd/MM");

        // organiza os valores utilizados pelo html
        for (Map<String, Object> venda : resumo.getVendasPorDia()) {

            String dataOriginal = (String) venda.get("data");

            LocalDate data = LocalDate.parse(dataOriginal);

            double faturamento =
                    ((Number) venda.get("faturamento")).doubleValue();

            double percentual = 0;

            if (maiorFaturamento > 0) {
                percentual = (faturamento * 100.0) / maiorFaturamento;
            }

            Map<String, Object> item = new HashMap<>();

            String diaSemana = data.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

            diaSemana = diaSemana.substring(0, 1).toUpperCase()
                    + diaSemana.substring(1);

         // apresenta a data de forma compacta
            item.put("data", data.format(formatadorData));
            item.put("valorFormatado", formatarMoeda(faturamento));
            item.put("percentual", percentual);

            vendas.add(item);
        }

        return vendas;
    }

    // prepara as formas de pagamento para o gráfico
    private List<Map<String, Object>> prepararFormasPagamento(
            ResumoAnaliseDTO resumo) {

        List<Map<String, Object>> pagamentos = new ArrayList<>();

        if (resumo.getFormasPagamento() == null) {
            return pagamentos;
        }

        long maiorQuantidade = 0;

        // identifica a maior quantidade de pagamentos
        for (Map<String, Object> pagamento : resumo.getFormasPagamento()) {

            long quantidade =
                    ((Number) pagamento.get("quantidade")).longValue();

            if (quantidade > maiorQuantidade) {
                maiorQuantidade = quantidade;
            }
        }

        // organiza os dados utilizados pelo html
        for (Map<String, Object> pagamento : resumo.getFormasPagamento()) {

            String nome = (String) pagamento.get("nomePagamento");

            long quantidade =
                    ((Number) pagamento.get("quantidade")).longValue();

            double percentual = 0;

            if (maiorQuantidade > 0) {
                percentual = (quantidade * 100.0) / maiorQuantidade;
            }

            Map<String, Object> item = new HashMap<>();

            item.put("nome", nome);
            item.put("quantidade", quantidade);
            item.put("percentual", percentual);

            pagamentos.add(item);
        }

        return pagamentos;
    }

}