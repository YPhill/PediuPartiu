package br.com.pediupartiu.model;

import jakarta.persistence.*;


//É nesta classe que os itens de cada pedido são representados, relacionando um produto à sua quantidade dentro do pedido.
@Entity
@Table(name = "Item_Pedido")
@IdClass(ItemPedidoId.class)
public class ItemPedido {

    //Atributos do item e seus relacionamentos com pedido e produto.
    @Id
    @ManyToOne
    @JoinColumn(name = "ID_Produto")
    private Produto produto;

    @Id
    @ManyToOne
    @JoinColumn(name = "ID_Pedido")
    private Pedido pedido;

    @Column(name = "Quantidade")
    private Integer quantidade;

    //Construtores da classe.
    public ItemPedido() {
    }

    public ItemPedido(Integer quantidade, Pedido pedido, Produto produto) {
        this.quantidade = quantidade;
        this.pedido = pedido;
        this.produto = produto;
    }

    //Gets e sets utilizados para acessar e alterar os dados do item do pedido.
    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}