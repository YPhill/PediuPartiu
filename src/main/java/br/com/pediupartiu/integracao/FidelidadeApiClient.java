
package br.com.pediupartiu.integracao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class FidelidadeApiClient {

    private final RestClient restClient;

    public FidelidadeApiClient() {
        this.restClient = RestClient.create("http://localhost:8082");
    }

    // Informa à Fidelidade que um pedido foi entregue.
    public void notificarPedidoEntregue(
            Integer idPedido,
            Integer idCliente) {

        restClient.post()
            .uri(uri -> uri
                .path("/api/fidelidade/teste/pedido-entregue")
                .queryParam("idPedido", idPedido)
                .queryParam("idCliente", idCliente)
                .build())
            .retrieve()
            .toBodilessEntity();
    }
    
    

    // Consulta o produto selecionado como brinde de fidelidade.
    public Integer consultarProdutoBrinde() {

        return restClient.get()
                .uri("/api/fidelidade/brinde")
                .retrieve()
                .body(Integer.class);
    }

    // Envia o produto escolhido para o microserviço de Fidelidade.
    public void selecionarProdutoBrinde(Integer idProduto) {

        restClient.post()
                .uri(uri -> uri
                        .path("/api/fidelidade/brinde")
                        .queryParam("idProduto", idProduto)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }
    
    

    // Consulta o progresso de fidelidade de um cliente.
    public Map<String, Object> consultarProgressoCliente(Integer idCliente) {

        return restClient.get()
                .uri("/api/fidelidade/clientes/{idCliente}", idCliente)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<
                        Map<String, Object>>() {});
    }


}
