package br.com.pediupartiu.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.pediupartiu.repository.ContatoRepository;

@RestController
@RequestMapping("/api/integracao")
public class ComunicacaoController {

    private final ContatoRepository contatoRepository;

    public ComunicacaoController(ContatoRepository contatoRepository) {
        this.contatoRepository = contatoRepository;
    }

    @GetMapping("/clientes/por-telefone")
    public ResponseEntity<?> buscarClientePorTelefone(
            @RequestParam String telefone) {

        // Mantém somente os números do telefone recebido.
        String telefoneNormalizado = telefone.replaceAll("[^0-9]", "");

        if (telefoneNormalizado.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Informe um telefone válido.");
        }

        List<Integer> ids = contatoRepository
                .buscarIdsClientesPorTelefone(telefoneNormalizado);

        if (ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhum cliente encontrado para esse telefone.");
        }

        if (ids.size() > 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Esse telefone está associado a mais de um cliente.");
        }

        return ResponseEntity.ok(
                new ClienteEncontradoResponse(ids.get(0)));
    }

    public record ClienteEncontradoResponse(Integer idCliente) {
    }
}