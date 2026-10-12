package br.com.pediupartiu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.Contato;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos contatos no banco de dados.
public interface ContatoRepository extends JpaRepository<Contato, Integer> {

    //Realiza a busca dos contatos vinculados a um cliente específico.
    List<Contato> findByCliente(Cliente cliente);
    

@Query(value = """
    SELECT DISTINCT c.ID_Cliente
    FROM contato c
    WHERE
        REGEXP_REPLACE(c.Telefone, '[^0-9]', '') =
            REGEXP_REPLACE(:telefone, '[^0-9]', '')
        OR
        REGEXP_REPLACE(c.Telefone, '[^0-9]', '') =
            CASE
                WHEN REGEXP_REPLACE(:telefone, '[^0-9]', '')
                     LIKE '55%'
                THEN SUBSTRING(
                    REGEXP_REPLACE(:telefone, '[^0-9]', ''),
                    3
                )
                ELSE REGEXP_REPLACE(:telefone, '[^0-9]', '')
            END
    """, nativeQuery = true)
List<Integer> buscarIdsClientesPorTelefone(
        @Param("telefone") String telefone);


}