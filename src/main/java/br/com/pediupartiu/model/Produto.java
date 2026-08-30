package br.com.pediupartiu.model;

import jakarta.persistence.*;

//É nesta classe que os produtos cadastrados no cardápio são representados, armazenando suas informações e categoria.
@Entity
@Table(name = "Produto")
public class Produto {

    //Atributos do produto e seu relacionamento com a categoria.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Produto")
    private Integer idProduto;

    @Column(name = "Nome_Produto", length = 100, nullable = false)
    private String nomeProduto;

    @ManyToOne
    @JoinColumn(name = "ID_Categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "Descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "Preco_Unitario", nullable = false)
    private Double precoUnitario;

    //Construtores da classe.
    public Produto() {
    }

    public Produto(String nomeProduto, Categoria categoria, String descricao, Double precoUnitario) {
        this.nomeProduto = nomeProduto;
        this.categoria = categoria;
        this.descricao = descricao;
        this.precoUnitario = precoUnitario;
    }

    //Gets e sets utilizados para acessar e alterar os dados do produto.
    public Integer getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Integer idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(Double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}