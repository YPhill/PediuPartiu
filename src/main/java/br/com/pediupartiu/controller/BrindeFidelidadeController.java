
package br.com.pediupartiu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.pediupartiu.integracao.FidelidadeApiClient;

@RestController
@RequestMapping("/api/brinde-fidelidade")
public class BrindeFidelidadeController {

    private final FidelidadeApiClient fidelidadeApiClient;

    public BrindeFidelidadeController(FidelidadeApiClient fidelidadeApiClient) {
        this.fidelidadeApiClient = fidelidadeApiClient;
    }

    // Consulta qual produto está selecionado como brinde.
    @GetMapping
    public ResponseEntity<Integer> consultarBrinde() {

        Integer idProduto = fidelidadeApiClient.consultarProdutoBrinde();

        if (idProduto == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(idProduto);
    }

    // Envia a seleção do produto para o microserviço.
    @PostMapping
    public ResponseEntity<String> selecionarBrinde(
            @RequestParam Integer idProduto) {

        fidelidadeApiClient.selecionarProdutoBrinde(idProduto);

        return ResponseEntity.ok("Brinde selecionado com sucesso.");
    }
}
