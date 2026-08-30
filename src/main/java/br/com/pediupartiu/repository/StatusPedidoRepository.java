package br.com.pediupartiu.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.pediupartiu.model.StatusPedido;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos status dos pedidos no banco de dados.
public interface StatusPedidoRepository extends JpaRepository<StatusPedido, Integer> {

    //Realiza a busca de um status utilizando o nome informado.
    StatusPedido findByNomeStatus(String nomeStatus);

    //Realiza a busca de um status utilizando o nome informado, desconsiderando letras maiúsculas e minúsculas.
    Optional<StatusPedido> findByNomeStatusIgnoreCase(String nomeStatus);

    //Retorna todos os status cadastrados ordenados pelo identificador.
    List<StatusPedido> findAllByOrderByIdStatusAsc();
}