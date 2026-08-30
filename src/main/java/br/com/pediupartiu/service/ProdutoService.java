package br.com.pediupartiu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pediupartiu.model.Categoria;
import br.com.pediupartiu.model.Produto;
import br.com.pediupartiu.repository.CategoriaRepository;
import br.com.pediupartiu.repository.ProdutoRepository;

//É nesta classe que ficam as regras de negócio relacionadas aos produtos do cardápio.
@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaRepository categoriaRepository) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    //Retorna todos os produtos cadastrados no sistema.
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto buscarPorId(Integer idProduto) {
        validarId(idProduto, "ID do produto inválido.");

        return produtoRepository.findById(idProduto)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    //Realiza a busca de um produto pelo nome informado.
    public Produto buscarPorNomeExato(String nomeProduto) {
        validarNome(nomeProduto);

        return produtoRepository.findByNomeProdutoIgnoreCase(nomeProduto.trim())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    //Realiza a busca de produtos utilizando parte do nome informado.
    public List<Produto> buscarPorNomeParcial(String nomeProduto) {
        validarNome(nomeProduto);

        return produtoRepository.findByNomeProdutoContainingIgnoreCase(nomeProduto.trim());
    }

    //Retorna os produtos pertencentes a uma categoria específica.
    public List<Produto> listarPorCategoria(Integer idCategoria) {
        Categoria categoria = buscarCategoriaPorId(idCategoria);
        return produtoRepository.findByCategoria(categoria);
    }

    //Retorna todas as categorias cadastradas no sistema.
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    //Realiza o cadastro de um novo produto no cardápio.
    @Transactional
    public Produto cadastrarProduto(String nomeProduto, Integer idCategoria, String descricao, Double precoUnitario) {
        validarDadosProduto(nomeProduto, idCategoria, precoUnitario);

        Categoria categoria = buscarCategoriaPorId(idCategoria);

        Produto produto = new Produto();
        produto.setNomeProduto(nomeProduto.trim());
        produto.setCategoria(categoria);
        produto.setDescricao(tratarTextoOpcional(descricao));
        produto.setPrecoUnitario(precoUnitario);

        return produtoRepository.save(produto);
    }

    //Atualiza os dados de um produto já cadastrado.
    @Transactional
    public Produto atualizarProduto(
            Integer idProduto,
            String nomeProduto,
            Integer idCategoria,
            String descricao,
            Double precoUnitario) {

        validarId(idProduto, "ID do produto inválido.");
        validarDadosProduto(nomeProduto, idCategoria, precoUnitario);

        Produto produto = buscarPorId(idProduto);
        Categoria categoria = buscarCategoriaPorId(idCategoria);

        produto.setNomeProduto(nomeProduto.trim());
        produto.setCategoria(categoria);
        produto.setDescricao(tratarTextoOpcional(descricao));
        produto.setPrecoUnitario(precoUnitario);

        return produtoRepository.save(produto);
    }

    //Realiza a exclusão de um produto do cardápio.
    @Transactional
    public void excluirProduto(Integer idProduto) {
        Produto produto = buscarPorId(idProduto);
        produtoRepository.delete(produto);
    }

    private Categoria buscarCategoriaPorId(Integer idCategoria) {
        validarId(idCategoria, "ID da categoria inválido.");

        return categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));
    }

    //Valida os dados informados para o cadastro ou edição do produto.
    private void validarDadosProduto(String nomeProduto, Integer idCategoria, Double precoUnitario) {
        validarNome(nomeProduto);
        validarId(idCategoria, "ID da categoria inválido.");

        if (precoUnitario == null || precoUnitario <= 0) {
            throw new IllegalArgumentException("O preço do produto deve ser maior que zero.");
        }
    }

    //Valida o nome informado para o produto.
    private void validarNome(String nomeProduto) {
        if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do produto.");
        }

        if (nomeProduto.trim().length() > 100) {
            throw new IllegalArgumentException("O nome do produto deve ter no máximo 100 caracteres.");
        }
    }

    //Valida se o identificador informado é válido.
    private void validarId(Integer id, String mensagemErro) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagemErro);
        }
    }

    private String tratarTextoOpcional(String texto) {
        return texto == null ? null : texto.trim();
    }
    
    //Realiza a busca de produtos utilizando o nome ou categoria informados.
    public List<Produto> buscarProdutos(String busca) {
        String termo = busca == null ? "" : busca.trim();

        if (termo.isEmpty()) {
            return listarTodos();
        }

        return produtoRepository.buscarPorNomeOuCategoria(termo);
    }
}