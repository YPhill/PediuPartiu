package br.com.pediupartiu.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.Pedido;
import java.time.LocalDateTime;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos pedidos no banco de dados.
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	//Realiza a busca dos pedidos vinculados a um cliente, ordenando do mais recente para o mais antigo.
	List<Pedido> findByClienteOrderByDataHoraPedidoDesc(Cliente cliente);

	//Retorna a quantidade de pedidos realizados em um determinado período.
    long countByDataHoraPedidoBetween(LocalDateTime inicio, LocalDateTime fim);

    //Retorna a quantidade de pedidos de um determinado status em um período informado.
    long countByDataHoraPedidoBetweenAndStatusPedido_NomeStatus(
            LocalDateTime inicio,
            LocalDateTime fim,
            String nomeStatus);
    
    //Realiza a busca de pedidos utilizando os filtros informados na tela.
    @Query("""
        SELECT p
        FROM Pedido p
        WHERE
            (
                :busca = ''
                OR LOWER(p.cliente.nomeCliente) LIKE LOWER(CONCAT('%', :busca, '%'))
                OR CAST(p.idPedido AS string) LIKE CONCAT('%', :busca, '%')
            )
            AND (
                :status = ''
                OR p.statusPedido.nomeStatus = :status
            )
            AND (
                :data = ''
                OR FUNCTION('DATE_FORMAT', p.dataHoraPedido, '%d/%m/%Y') = :data
            )
    ORDER BY p.dataHoraPedido DESC """)
    List<Pedido> buscarPedidos(
            @Param("busca") String busca,
            @Param("status") String status,
            @Param("data") String data);
    
    //Calcula o faturamento obtido dentro do período informado.
    @Query("""
    	    SELECT SUM(i.quantidade * i.produto.precoUnitario)
    	    FROM ItemPedido i
    	    WHERE i.pedido.dataHoraPedido BETWEEN :inicio AND :fim
    	""")
    	Double calcularFaturamentoEntre(
    	        @Param("inicio") LocalDateTime inicio,
    	        @Param("fim") LocalDateTime fim);

    //Retorna as formas de pagamento mais utilizadas em um determinado período.
    	@Query("""
    	    SELECT p.formaPagamento.nomeFormaPagamento, COUNT(p)
    	    FROM Pedido p
    	    WHERE p.dataHoraPedido BETWEEN :inicio AND :fim
    	      AND p.formaPagamento IS NOT NULL
    	    GROUP BY p.formaPagamento.nomeFormaPagamento
    	    ORDER BY COUNT(p) DESC
    	""")
    	List<Object[]> buscarFormasPagamentoDashboard(
    	        @Param("inicio") LocalDateTime inicio,
    	        @Param("fim") LocalDateTime fim);
}