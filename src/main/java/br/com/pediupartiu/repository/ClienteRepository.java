package br.com.pediupartiu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.pediupartiu.model.Cliente;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos clientes no banco de dados.
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    //Realiza a busca de clientes utilizando o nome ou telefone informado.
    @Query("""
        SELECT DISTINCT c
        FROM Cliente c
        LEFT JOIN c.contatos contato
        WHERE LOWER(c.nomeCliente) LIKE LOWER(CONCAT('%', :busca, '%'))
           OR contato.telefone LIKE CONCAT('%', :busca, '%')
    """)
    List<Cliente> buscarPorNomeOuTelefone(@Param("busca") String busca);
}