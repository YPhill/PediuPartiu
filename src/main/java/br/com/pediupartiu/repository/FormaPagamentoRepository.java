package br.com.pediupartiu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.pediupartiu.model.FormaPagamento;
import java.util.Optional;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão das formas de pagamento no banco de dados.
public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Integer> {
	
	//Realiza a busca de uma forma de pagamento pelo nome informado.
	Optional<FormaPagamento> findByNomeFormaPagamentoIgnoreCase(String nomeFormaPagamento);

}