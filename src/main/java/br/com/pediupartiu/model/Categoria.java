package br.com.pediupartiu.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

//É nesta classe que as categorias dos produtos do cardápio são representadas.
@Entity
@Table(name = "Categoria")
public class Categoria {

    //Atributos da categoria.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Categoria")
    private Integer idCategoria;

    @Column(name = "Nome_Categoria", length = 50, nullable = false)
    private String nomeCategoria;

    //Gets e sets utilizados para acessar e alterar os atributos da categoria.
    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }
}