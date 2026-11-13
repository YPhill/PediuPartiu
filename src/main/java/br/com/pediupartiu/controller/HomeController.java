
package br.com.pediupartiu.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.pediupartiu.integracao.analise.AnaliseApiClient;
import br.com.pediupartiu.integracao.analise.ResumoAnaliseDTO;

@Controller
public class HomeController {

    private final AnaliseApiClient analiseApiClient;

    public HomeController(AnaliseApiClient analiseApiClient) {
        this.analiseApiClient = analiseApiClient;
    }

    // carrega os indicadores da tela inicial
    @GetMapping("/inicio")
    public String telaInicial(Model model, HttpSession session) {

        // verifica se o usuário possui acesso válido
        String perfilUsuario = (String) session.getAttribute("perfilUsuario");

        if (!"ADM".equals(perfilUsuario)
                && !"COZINHA".equals(perfilUsuario)) {
            return "redirect:/";
        }

        String dataHoje = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        model.addAttribute("dataHoje", dataHoje);

        try {

            // consulta os indicadores de hoje na análise
            ResumoAnaliseDTO resumo =
                    analiseApiClient.consultarIndicadores(0);

            if (resumo == null) {
                throw new IllegalStateException(
                        "resumo de análise indisponível");
            }

            // disponibiliza os indicadores para o html
            model.addAttribute(
                    "totalPedidosHoje",
                    resumo.getPedidosRegistrados()
            );

            model.addAttribute(
                    "pedidosPendentesHoje",
                    resumo.getPedidosPendentes()
            );

            model.addAttribute(
                    "pedidosConcluidosHoje",
                    resumo.getPedidosEntregues()
            );

            model.addAttribute(
                    "faturamentoHoje",
                    String.format(
                            Locale.forLanguageTag("pt-BR"),
                            "%.2f",
                            resumo.getFaturamento()
                    )
            );

            model.addAttribute("analiseDisponivel", true);

        } catch (Exception e) {

            // mantém a tela inicial disponível
            model.addAttribute("analiseDisponivel", false);

            model.addAttribute(
                    "erro",
                    "Os indicadores do Resumo do Dia estão temporariamente indisponíveis."
            );

        }

        return "index";
    }
}
