package br.com.pediupartiu.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.pediupartiu.model.ItemPedido;
import br.com.pediupartiu.model.ItemPedidoId;
import br.com.pediupartiu.model.Pedido;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos itens dos pedidos no banco de dados.
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, ItemPedidoId> {

    //Realiza a busca dos itens vinculados a um pedido específico.
    List<ItemPedido> findByPedido(Pedido pedido);

    //Retorna a quantidade de itens cadastrados em um determinado pedido.
    long countByPedido(Pedido pedido);
    
    //Realiza a busca dos produtos mais pedidos em um período informado.
    @Query("""
    	    SELECT i.produto.nomeProduto, SUM(i.quantidade)
    	    FROM ItemPedido i
    	    WHERE i.pedido.dataHoraPedido BETWEEN :inicio AND :fim
    	    GROUP BY i.produto.nomeProduto
    	    ORDER BY SUM(i.quantidade) DESC
    	""")
    	List<Object[]> buscarItensMaisPedidos(LocalDateTime inicio, LocalDateTime fim);  

}