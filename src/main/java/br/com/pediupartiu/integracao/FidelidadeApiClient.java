
package br.com.pediupartiu.integracao;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;

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
    
    
 // solicita o resgate do brinde ao microserviço
    public void resgatarBrinde(
            Integer idPedido,
            Integer idCliente,
            Integer idProdutoBrinde,
            BigDecimal valorDesconto) {

        restClient.post()
                .uri(uri -> uri
                        .path("/api/fidelidade/resgatar")
                        .queryParam("idPedido", idPedido)
                        .queryParam("idCliente", idCliente)
                        .queryParam("idProdutoBrinde", idProdutoBrinde)
                        .queryParam("valorDesconto", valorDesconto)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }
    
 // consulta se o pedido já teve o brinde resgatado
    public boolean consultarResgatePedido(Integer idPedido) {

        Boolean resgatado = restClient.get()
                .uri("/api/fidelidade/resgates/pedidos/{idPedido}", idPedido)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(resgatado);
    }

    
 // consulta o desconto registrado para um pedido
    public BigDecimal consultarDescontoPedido(Integer idPedido) {

        return restClient.get()
                .uri("/api/fidelidade/resgates/pedidos/{idPedido}/desconto", idPedido)
                .retrieve()
                .body(BigDecimal.class);
    }
    
 // consulta o produto utilizado como brinde em um pedido
    public Integer consultarProdutoBrindePedido(Integer idPedido) {

        return restClient.get()
                .uri("/api/fidelidade/resgates/pedidos/{idPedido}/produto",
                        idPedido)
                .retrieve()
                .body(Integer.class);
    }
    
 // solicita o estorno de um pedido contabilizado
    public void estornarPedido(Integer idPedido) {

        restClient.post()
                .uri("/api/fidelidade/pedidos/{idPedido}/estornar", idPedido)
                .retrieve()
                .toBodilessEntity();
    }
    

    // solicita a devolução do brinde de um pedido cancelado
    public void devolverBrinde(Integer idPedido) {

        restClient.post()
                .uri("/api/fidelidade/resgates/pedidos/{idPedido}/devolver",
                        idPedido)
                .retrieve()
                .toBodilessEntity();
    }
    
 // consulta se o brinde já foi devolvido
    public boolean consultarBrindeDevolvido(Integer idPedido) {

        Boolean devolvido = restClient.get()
                .uri("/api/fidelidade/resgates/pedidos/{idPedido}/devolvido",
                        idPedido)
                .retrieve()
                .body(Boolean.class);

        return Boolean.TRUE.equals(devolvido);
    }


}
