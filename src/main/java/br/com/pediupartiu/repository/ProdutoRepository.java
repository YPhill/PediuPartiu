package br.com.pediupartiu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.pediupartiu.model.Categoria;
import br.com.pediupartiu.model.Produto;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos produtos no banco de dados.
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    //Realiza a busca dos produtos pertencentes a uma categoria específica.
    List<Produto> findByCategoria(Categoria categoria);

    //Realiza a busca de um produto pelo nome informado.
    Optional<Produto> findByNomeProdutoIgnoreCase(String nomeProduto);

    //Realiza a busca de produtos que contenham o nome informado.
    List<Produto> findByNomeProdutoContainingIgnoreCase(String nomeProduto);

    //Realiza a busca de produtos utilizando o nome do produto ou da categoria.
    @Query("""
        SELECT p
        FROM Produto p
        JOIN p.categoria c
        WHERE LOWER(p.nomeProduto) LIKE LOWER(CONCAT('%', :busca, '%'))
           OR LOWER(c.nomeCategoria) LIKE LOWER(CONCAT('%', :busca, '%'))
    """)
    List<Produto> buscarPorNomeOuCategoria(@Param("busca") String busca);
}