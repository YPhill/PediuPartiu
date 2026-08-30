package br.com.pediupartiu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pediupartiu.model.ItemPedido;
import br.com.pediupartiu.model.ItemPedidoId;
import br.com.pediupartiu.model.Pedido;
import br.com.pediupartiu.model.Produto;
import br.com.pediupartiu.repository.ItemPedidoRepository;
import br.com.pediupartiu.repository.PedidoRepository;
import br.com.pediupartiu.repository.ProdutoRepository;
import java.time.LocalDateTime;

//É nesta classe que ficam as regras de negócio relacionadas aos itens dos pedidos.
@Service
public class ItemPedidoService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public ItemPedidoService(
            ItemPedidoRepository itemPedidoRepository,
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }

    //Retorna todos os itens de pedidos cadastrados no sistema.
    public List<ItemPedido> listarTodos() {
        return itemPedidoRepository.findAll();
    }

    //Realiza a busca dos itens pertencentes a um pedido específico.
    public List<ItemPedido> buscarItensPorPedido(Integer idPedido) {
        Pedido pedido = buscarPedidoPorId(idPedido);
        return itemPedidoRepository.findByPedido(pedido);
    }

    //Retorna a quantidade de itens cadastrados em um pedido.
    public long contarItens(Integer idPedido) {
        Pedido pedido = buscarPedidoPorId(idPedido);
        return itemPedidoRepository.countByPedido(pedido);
    }

    //Adiciona um novo item ao pedido ou atualiza sua quantidade caso ele já exista.
    @Transactional
    public ItemPedido adicionarItemAoPedido(Integer idPedido, Integer idProduto, Integer quantidade) {
        validarQuantidade(quantidade);

        Pedido pedido = buscarPedidoPorId(idPedido);
        Produto produto = buscarProdutoPorId(idProduto);

        ItemPedidoId id = new ItemPedidoId(idProduto, idPedido);

        ItemPedido item = itemPedidoRepository.findById(id).orElse(null);

        if (item == null) {
            item = new ItemPedido();
            item.setPedido(pedido);
            item.setProduto(produto);
            item.setQuantidade(quantidade);
        } else {
            item.setQuantidade(item.getQuantidade() + quantidade);
        }

        return itemPedidoRepository.save(item);
    }

    //Atualiza a quantidade de um item já existente no pedido.
    @Transactional
    public ItemPedido atualizarQuantidade(Integer idPedido, Integer idProduto, Integer novaQuantidade) {
        validarQuantidade(novaQuantidade);

        ItemPedido item = buscarItemPorIdComposto(idPedido, idProduto);
        item.setQuantidade(novaQuantidade);

        return itemPedidoRepository.save(item);
    }

    //Realiza a exclusão de um item do pedido.
    @Transactional
    public void excluirItem(Integer idPedido, Integer idProduto) {
        ItemPedido item = buscarItemPorIdComposto(idPedido, idProduto);
        itemPedidoRepository.delete(item);
    }

    //Calcula o valor total de um item considerando quantidade e preço unitário.
    public Double calcularTotalItem(ItemPedido item) {
        if (item == null 
                || item.getQuantidade() == null
                || item.getProduto() == null 
                || item.getProduto().getPrecoUnitario() == null) {
            return 0.0;
        }

        return item.getQuantidade() * item.getProduto().getPrecoUnitario();
    }

    //Calcula o valor total de um pedido somando todos os seus itens.
    public Double calcularTotalPedido(Integer idPedido) {
        return buscarItensPorPedido(idPedido)
                .stream()
                .mapToDouble(this::calcularTotalItem)
                .sum();
    }

    private ItemPedido buscarItemPorIdComposto(Integer idPedido, Integer idProduto) {
        validarId(idPedido, "ID do pedido inválido.");
        validarId(idProduto, "ID do produto inválido.");

        ItemPedidoId id = new ItemPedidoId(idProduto, idPedido);

        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item do pedido não encontrado."));
    }

    private Pedido buscarPedidoPorId(Integer idPedido) {
        validarId(idPedido, "ID do pedido inválido.");

        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
    }

    private Produto buscarProdutoPorId(Integer idProduto) {
        validarId(idProduto, "ID do produto inválido.");

        return produtoRepository.findById(idProduto)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    //Valida a quantidade informada para o item do pedido.
    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
    }

    //Valida se o identificador informado é válido.
    private void validarId(Integer id, String mensagemErro) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagemErro);
        }
    }

    //Retorna os produtos mais pedidos em um determinado período.
    public List<Object[]> buscarItensMaisPedidos(LocalDateTime inicio, LocalDateTime fim) {
        return itemPedidoRepository.buscarItensMaisPedidos(inicio, fim);
    }
}