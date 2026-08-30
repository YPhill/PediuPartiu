package br.com.pediupartiu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.Contato;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão dos contatos no banco de dados.
public interface ContatoRepository extends JpaRepository<Contato, Integer> {

    //Realiza a busca dos contatos vinculados a um cliente específico.
    List<Contato> findByCliente(Cliente cliente);

}