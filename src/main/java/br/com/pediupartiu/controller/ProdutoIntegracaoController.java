package br.com.pediupartiu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pediupartiu.model.Produto;
import br.com.pediupartiu.service.ProdutoService;

import java.util.Map;

// Disponibiliza a consulta de produtos para o microserviço Fidelidade.
@RestController
@RequestMapping("/api/integracao/produtos")
public class ProdutoIntegracaoController {

    private final ProdutoService produtoService;

    public ProdutoIntegracaoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // Verifica se o produto existe no cardápio.
    @GetMapping("/{idProduto}/existe")
    public ResponseEntity<Void> verificarProduto(
            @PathVariable Integer idProduto) {

        try {
            Produto produto = produtoService.buscarPorId(idProduto);

            if (produto != null) {
                return ResponseEntity.ok().build();
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.notFound().build();
    }
    

    // Consulta os dados do produto para a integração com a Fidelidade.
    @GetMapping("/{idProduto}")
    public ResponseEntity<Map<String, Object>> consultarProduto(
            @PathVariable Integer idProduto) {

        try {
            Produto produto = produtoService.buscarPorId(idProduto);

            if (produto == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> dados = Map.of(
                    "idProduto", produto.getIdProduto(),
                    "nomeProduto", produto.getNomeProduto(),
                    "precoUnitario", produto.getPrecoUnitario()
            );

            return ResponseEntity.ok(dados);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

}