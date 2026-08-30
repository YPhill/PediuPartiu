package br.com.pediupartiu.controller;

import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import br.com.pediupartiu.model.Categoria;
import br.com.pediupartiu.model.Produto;
import br.com.pediupartiu.service.ProdutoService;

//É nesta classe que todas as operações relacionadas ao cardápio são controladas, como cadastro, edição, busca e exclusão dos produtos.
@Controller
@RequestMapping("/cardapio")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    //Apresenta a tela do cardápio e permite realizar buscas pelos produtos cadastrados.
    @GetMapping
    public String telaCardapio(
            @RequestParam(required = false) String busca,
            Model model) {

        List<Produto> produtos;

        if (busca != null && !busca.trim().isEmpty()) {
            produtos = produtoService.buscarProdutos(busca);
        } else {
            produtos = produtoService.listarTodos();
        }

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", produtoService.listarCategorias());
        model.addAttribute("busca", busca);

        return "produto/cardapio";
    }

    //Realiza a busca de produtos pelo nome informado.
    @GetMapping("/buscar")
    public String buscarProduto(
            @RequestParam String nomeProduto,
            Model model) {

        List<Produto> produtos = produtoService.buscarPorNomeParcial(nomeProduto);
        List<Categoria> categorias = produtoService.listarCategorias();

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", categorias);

        return "produto/cardapio";
    }

    //Busca os dados de um produto já cadastrado para preenchimento da tela de edição.
    @GetMapping("/editar/{idProduto}")
    public String editarProduto(
            @PathVariable Integer idProduto,
            Model model) {

        Produto produto = produtoService.buscarPorId(idProduto);

        carregarDadosCardapio(model);
        model.addAttribute("produtoEdicao", produto);

        return "produto/cardapio";
    }

    //Recebe os dados informados e realiza o cadastro de um novo item no cardápio.
    @PostMapping("/salvar")
    public String salvarProduto(
            @RequestParam String nomeProduto,
            @RequestParam Integer idCategoria,
            @RequestParam Double precoUnitario,
            @RequestParam(required = false) String descricao,
            RedirectAttributes redirectAttributes) {

        produtoService.cadastrarProduto(
                nomeProduto,
                idCategoria,
                descricao,
                precoUnitario
        );

        redirectAttributes.addFlashAttribute("sucesso", "Item do cardápio criado com sucesso!");
        return "redirect:/cardapio";
    }

    //Atualiza as informações de um produto já existente no cardápio.
    @PostMapping("/atualizar")
    public String atualizarProduto(
            @RequestParam Integer idProduto,
            @RequestParam String nomeProduto,
            @RequestParam Integer idCategoria,
            @RequestParam Double precoUnitario,
            @RequestParam(required = false) String descricao,
            RedirectAttributes redirectAttributes) {

        produtoService.atualizarProduto(
                idProduto,
                nomeProduto,
                idCategoria,
                descricao,
                precoUnitario
        );

        redirectAttributes.addFlashAttribute("sucesso", "Item do cardápio atualizado com sucesso!");
        return "redirect:/cardapio";
    }

    //Realiza a exclusão de um item do cardápio.
    @PostMapping("/excluir")
    public String excluirProduto(
            @RequestParam Integer idProduto,
            RedirectAttributes redirectAttributes) {

        produtoService.excluirProduto(idProduto);

        redirectAttributes.addFlashAttribute("sucesso", "Item do cardápio excluído com sucesso!");

        return "redirect:/cardapio";
    }

    //Exibe apenas os produtos pertencentes à categoria selecionada.
    @GetMapping("/categoria/{idCategoria}")
    public String listarPorCategoria(
            @PathVariable Integer idCategoria,
            Model model) {

        List<Produto> produtos = produtoService.listarPorCategoria(idCategoria);
        List<Categoria> categorias = produtoService.listarCategorias();

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", categorias);

        return "produto/cardapio";
    }

    //Retorna para a tela inicial do sistema.
    @GetMapping("/inicio")
    public String voltarInicio() {
        return "redirect:/inicio";
    }

    //Carrega os dados necessários para exibição da tela do cardápio.
    private void carregarDadosCardapio(Model model) {
        List<Produto> produtos = produtoService.listarTodos();
        List<Categoria> categorias = produtoService.listarCategorias();

        model.addAttribute("produtos", produtos);
        model.addAttribute("categorias", categorias);
    }
}