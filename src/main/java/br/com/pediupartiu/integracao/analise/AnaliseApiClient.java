package br.com.pediupartiu.integracao.analise;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AnaliseApiClient {

    private final RestClient restClient;

    public AnaliseApiClient(
            @Value("${analise.api.url}") String enderecoApi) {

        this.restClient = RestClient.create(enderecoApi);
    }

    // consulta o resumo completo do microsserviço
    public String consultarResumo(Integer dias) {

        String endereco = "/api/analise/resumo";

        if (dias != null) {
            endereco += "?dias=" + dias;
        }

        return restClient.get()
                .uri(endereco)
                .retrieve()
                .body(String.class);
    }

    // consulta os indicadores para utilização no dashboard
    public ResumoAnaliseDTO consultarIndicadores(Integer dias) {

        String endereco = "/api/analise/resumo";

        if (dias != null) {
            endereco += "?dias=" + dias;
        }

        return restClient.get()
                .uri(endereco)
                .retrieve()
                .body(ResumoAnaliseDTO.class);
    }
}