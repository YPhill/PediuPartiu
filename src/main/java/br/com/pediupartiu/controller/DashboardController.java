package br.com.pediupartiu.controller;

import java.text.NumberFormat;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.pediupartiu.service.PedidoService;

//É nesta classe que os dados apresentados no dashboard são carregados, exibindo indicadores e gráficos para acompanhamento do negócio.
@Controller
public class DashboardController {

    private final PedidoService pedidoService;

    public DashboardController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    //Busca os principais indicadores do sistema e envia as informações para a tela do dashboard.
    @GetMapping("/dashboard")
    public String telaDashboard(Model model) {

        try {

            // INDICADORES - ÚLTIMOS 7 DIAS

            double faturamento7Dias =
                    pedidoService.calcularFaturamentoUltimos7Dias();

            double ticketMedio7Dias =
                    pedidoService.calcularTicketMedioUltimos7Dias();

            // FORMATAR MOEDA

            model.addAttribute(
                    "faturamentoDiaFormatado",
                    formatarMoeda(faturamento7Dias)
            );

            model.addAttribute(
                    "ticketMedioFormatado",
                    formatarMoeda(ticketMedio7Dias)
            );

            // GRÁFICOS

            model.addAttribute(
                    "itensMaisPedidos",
                    pedidoService.buscarItensMaisPedidosDashboard()
            );

            model.addAttribute(
                    "formasPagamentoDashboard",
                    pedidoService.buscarFormasPagamentoDashboard()
            );

            return "dashboard/dashboard";

        } catch (Exception e) {

            model.addAttribute(
                    "erro",
                    "Banco de dados temporariamente indisponível. Tente novamente."
            );

            return "dashboard/dashboard";
        }
    }

    //Recebe um valor numérico e realiza sua formatação para o padrão monetário brasileiro.
    private String formatarMoeda(double valor) {

        NumberFormat formatador =
                NumberFormat.getCurrencyInstance(
                        new Locale("pt", "BR")
                );

        return formatador.format(valor);
    }
}