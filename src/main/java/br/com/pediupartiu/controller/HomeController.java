package br.com.pediupartiu.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.pediupartiu.service.PedidoService;

//É nesta classe que as informações exibidas na tela inicial são carregadas, apresentando um resumo das operações do dia.
@Controller
public class HomeController {

    private final PedidoService pedidoService;

    public HomeController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    //Busca os principais indicadores do dia e envia essas informações para a tela inicial do sistema.
    @GetMapping("/inicio")
    public String telaInicial(Model model) {

        try {

            String dataHoje = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            long totalPedidosHoje = pedidoService.contarPedidosHoje();

            long pedidosPendentesHoje =
                    pedidoService.contarPedidosHojePorStatus("Pendente");

            long pedidosConcluidosHoje =
                    pedidoService.contarPedidosHojePorStatus("Entregue");
            double faturamentoHoje = pedidoService.calcularFaturamentoHoje();

            model.addAttribute("dataHoje", dataHoje);

            model.addAttribute("totalPedidosHoje", totalPedidosHoje);

            model.addAttribute("pedidosPendentesHoje", pedidosPendentesHoje);

            model.addAttribute("pedidosConcluidosHoje", pedidosConcluidosHoje);
            model.addAttribute("faturamentoHoje", String.format("%.2f", faturamentoHoje).replace(".", ","));

            return "index";

        } catch (Exception e) {

            model.addAttribute(
                    "erro",
                    "Banco de dados temporariamente indisponível. Tente novamente."
            );

            return "index";
        }
    }
}