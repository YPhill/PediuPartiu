
package br.com.pediupartiu.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import br.com.pediupartiu.integracao.FidelidadeApiClient;

@RestController
@RequestMapping("/api/consulta-fidelidade")
public class ConsultaFidelidadeController {

    private final FidelidadeApiClient fidelidadeApiClient;

    public ConsultaFidelidadeController(
            FidelidadeApiClient fidelidadeApiClient) {
        this.fidelidadeApiClient = fidelidadeApiClient;
    }

    // Consulta o progresso do cliente no microserviço de Fidelidade.
    @GetMapping("/clientes/{idCliente}")
    public ResponseEntity<?> consultarCliente(
            @PathVariable Integer idCliente) {

        try {
            Map<String, Object> progresso =
                    fidelidadeApiClient.consultarProgressoCliente(idCliente);

            if (progresso == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(progresso);

        } catch (HttpClientErrorException.NotFound e) {
            // Cliente ainda não possui registro na Fidelidade.
            return ResponseEntity.noContent().build();

        } catch (RestClientException e) {
            // Não informa que o cliente não possui brinde
            // quando o microserviço está indisponível.
            return ResponseEntity.status(503)
                    .body("Não foi possível consultar a Fidelidade.");
        }
    }
}
