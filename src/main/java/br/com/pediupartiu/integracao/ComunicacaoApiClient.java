
package br.com.pediupartiu.integracao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ComunicacaoApiClient {

    private final RestClient restClient;

    public ComunicacaoApiClient() {
        this.restClient = RestClient.create("http://localhost:8081");
    }

    public void notificarStatusPedido(
            Integer idPedido,
            Integer idCliente,
            Integer idStatus) {

        NotificacaoStatusRequest requisicao =
            new NotificacaoStatusRequest(
                idPedido,
                idCliente,
                idStatus
            );

        restClient.post()
            .uri("/api/comunicacao/pedido/status")
            .body(requisicao)
            .retrieve()
            .toBodilessEntity();
    }

    public record NotificacaoStatusRequest(
        Integer idPedido,
        Integer idCliente,
        Integer idStatus
    ) {}
}
