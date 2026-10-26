package br.com.pediupartiu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.pediupartiu.integracao.analise.AnaliseApiClient;

@RestController
public class AnaliseIntegracaoTesteController {

    private final AnaliseApiClient analiseApiClient;

    public AnaliseIntegracaoTesteController(
            AnaliseApiClient analiseApiClient) {

        this.analiseApiClient = analiseApiClient;
    }

    // testa a comunicação com o microsserviço de análise
    @GetMapping("/api/integracao/analise/teste")
    public String testarIntegracao(
            @RequestParam(required = false) Integer dias) {

        return analiseApiClient.consultarResumo(dias);
    }
}