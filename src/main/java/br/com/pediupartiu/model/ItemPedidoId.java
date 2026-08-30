package br.com.pediupartiu.model;

import java.io.Serializable;
import java.util.Objects;

//É nesta classe que a chave composta utilizada pela entidade ItemPedido é representada.
public class ItemPedidoId implements Serializable {
	private static final long serialVersionUID = 1L;

    //Atributos que identificam de forma única cada item do pedido.
    private Integer produto;
    private Integer pedido;

    //Construtores da classe.
    public ItemPedidoId() {
    }

    public ItemPedidoId(Integer produto, Integer pedido) {
        this.produto = produto;
        this.pedido = pedido;
    }

    //Métodos utilizados para comparar e identificar corretamente os registros da chave composta.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPedidoId)) return false;
        ItemPedidoId that = (ItemPedidoId) o;
        return Objects.equals(produto, that.produto) &&
               Objects.equals(pedido, that.pedido);
    }

    @Override
    public int hashCode() {
        return Objects.hash(produto, pedido);
    }
}