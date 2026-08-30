package br.com.pediupartiu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.pediupartiu.model.Categoria;

//É nesta interface que são realizadas as operações de cadastro, consulta, alteração e exclusão das categorias no banco de dados.
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

}