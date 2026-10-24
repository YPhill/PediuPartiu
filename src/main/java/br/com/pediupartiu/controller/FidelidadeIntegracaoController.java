
package br.com.pediupartiu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.pediupartiu.repository.PedidoRepository;

import java.util.Map;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping("/api/integracao/fidelidade")
public class FidelidadeIntegracaoController {

    private final PedidoRepository pedidoRepository;

    public FidelidadeIntegracaoController(
            PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // Verifica se a integração está disponível.
    @GetMapping("/status")
    public String verificarStatus() {
        return "Integração Fidelidade disponível";
    }



    // Retorna os pedidos entregues desde o início da promoção. 19/10/2026, às 00h00.
    @GetMapping("/pedidos-entregues")
    public List<Map<String, Object>> listarPedidosEntregues() {

        LocalDateTime inicioPromocao =
                LocalDateTime.of(2026, 10, 19, 0, 0);

        return pedidoRepository.findByStatusPedido_IdStatus(8)
                .stream()
                .filter(pedido ->
                        pedido.getDataHoraPedido() != null
                        && !pedido.getDataHoraPedido()
                                .isBefore(inicioPromocao))
                .map(pedido -> Map.<String, Object>of(
                        "idPedido", pedido.getIdPedido(),
                        "idCliente", pedido.getCliente().getIdCliente(),
                        "dataHoraPedido", pedido.getDataHoraPedido().toString()
                ))
                .toList();
    }


    
    
    

    // Consulta os dados necessários para validar o resgate.
    @GetMapping("/pedidos/{idPedido}")
    public ResponseEntity<Map<String, Object>> consultarPedido(
            @PathVariable Integer idPedido) {

        return pedidoRepository.findById(idPedido)
                .map(pedido -> {

                    List<Map<String, Object>> itens = pedido.getItens()
                            .stream()
                            .map(item -> Map.<String, Object>of(
                                    "idProduto", item.getProduto().getIdProduto(),
                                    "quantidade", item.getQuantidade(),
                                    "precoUnitario", item.getProduto().getPrecoUnitario()
                            ))
                            .toList();

                    Map<String, Object> dados = Map.of(
                            "idPedido", pedido.getIdPedido(),
                            "idCliente", pedido.getCliente().getIdCliente(),
                            "itens", itens
                    );

                    return ResponseEntity.ok(dados);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    
}
