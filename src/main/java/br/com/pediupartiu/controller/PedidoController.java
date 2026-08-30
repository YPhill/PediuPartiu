package br.com.pediupartiu.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.FormaPagamento;
import br.com.pediupartiu.model.ItemPedido;
import br.com.pediupartiu.model.Pedido;
import br.com.pediupartiu.model.Produto;
import br.com.pediupartiu.model.StatusPedido;
import br.com.pediupartiu.repository.ClienteRepository;
import br.com.pediupartiu.repository.FormaPagamentoRepository;
import br.com.pediupartiu.repository.StatusPedidoRepository;
import br.com.pediupartiu.service.ItemPedidoService;
import br.com.pediupartiu.service.PedidoService;
import br.com.pediupartiu.service.ProdutoService;

//É nesta classe que todas as operações relacionadas aos pedidos são controladas, desde o cadastro até a edição, busca e exclusão.
@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ItemPedidoService itemPedidoService;
    private final ProdutoService produtoService;
    private final ClienteRepository clienteRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final StatusPedidoRepository statusPedidoRepository;

    public PedidoController(
            PedidoService pedidoService,
            ItemPedidoService itemPedidoService,
            ProdutoService produtoService,
            ClienteRepository clienteRepository,
            FormaPagamentoRepository formaPagamentoRepository,
            StatusPedidoRepository statusPedidoRepository) {
        this.pedidoService = pedidoService;
        this.itemPedidoService = itemPedidoService;
        this.produtoService = produtoService;
        this.clienteRepository = clienteRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.statusPedidoRepository = statusPedidoRepository;
    }

    //Apresenta a tela de pedidos e permite realizar buscas utilizando os filtros disponíveis.
    @GetMapping
    public String telaPedidos(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String data,
            Model model) {

        carregarDadosTelaPedidos(model);

        List<Pedido> pedidos = pedidoService.buscarPedidos(busca, status, data);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("busca", busca);
        model.addAttribute("status", status);
        model.addAttribute("data", data);

        return "pedido/pedidos";
    }

    //Busca os dados de um pedido já existente para preenchimento da tela de edição.
    @GetMapping("/editar/{idPedido}")
    public String editarPedido(@PathVariable Integer idPedido, Model model) {
        Pedido pedido = pedidoService.buscarPorId(idPedido);
        List<ItemPedido> itens = itemPedidoService.buscarItensPorPedido(idPedido);

        carregarDadosTelaPedidos(model);
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("pedidoEdicao", pedido);
        model.addAttribute("itensEdicao", itens);
        model.addAttribute("totalPedido", itemPedidoService.calcularTotalPedido(idPedido));

        return "pedido/pedidos";
    }

    //Recebe os dados informados no cadastro e realiza a criação de um novo pedido.
    @PostMapping("/salvar")
    public String salvarPedido(
            @RequestParam Integer idCliente,
            @RequestParam Integer idPagamento,
            @RequestParam(required = false) String observacao,
            @RequestParam List<Integer> produtos,
            @RequestParam List<Integer> quantidades,
            RedirectAttributes redirectAttributes) {

        Map<Integer, Integer> itens = montarMapaItens(produtos, quantidades);

        pedidoService.criarPedidoCompleto(
                idCliente,
                idPagamento,
                observacao,
                itens
        );

        redirectAttributes.addFlashAttribute("sucesso", "Pedido cadastrado com sucesso!");
        return "redirect:/pedidos";
    }

    //Atualiza as informações de um pedido já cadastrado.
    @PostMapping("/atualizar")
    public String atualizarPedido(
            @RequestParam Integer idPedido,
            @RequestParam(required = false) Integer idCliente,
            @RequestParam(required = false) List<Integer> produtos,
            @RequestParam(required = false) List<Integer> quantidades,
            @RequestParam(required = false) String nomeStatus,
            @RequestParam(required = false) String nomeFormaPagamento,
            @RequestParam(required = false) String observacao,
            @RequestParam(required = false, defaultValue = "false") Boolean atualizarDataHoraPedido,
            @RequestParam(required = false, defaultValue = "false") Boolean voltarStatusPendente,
            RedirectAttributes redirectAttributes) {

        Map<Integer, Integer> itens = null;

        if (produtos != null && quantidades != null) {
            itens = montarMapaItens(produtos, quantidades);
        }

        pedidoService.editarPedidoCompleto(
                idPedido,
                idCliente,
                nomeStatus,
                nomeFormaPagamento,
                observacao,
                itens,
                Boolean.TRUE.equals(atualizarDataHoraPedido),
                Boolean.TRUE.equals(voltarStatusPendente)
        );

        redirectAttributes.addFlashAttribute("sucesso", "Pedido atualizado com sucesso!");
        return "redirect:/pedidos";
    }

    //Atualiza a quantidade de um item já existente no pedido.
    @PostMapping("/item/atualizar")
    public String atualizarItem(
            @RequestParam Integer idPedido,
            @RequestParam Integer idProduto,
            @RequestParam Integer quantidade) {

        pedidoService.atualizarQuantidadeItem(idPedido, idProduto, quantidade);

        return "redirect:/pedidos/editar/" + idPedido;
    }

    //Remove um item específico ou o pedido inteiro quando necessário.
    @PostMapping("/item/excluir")
    public String excluirItemOuPedido(
            @RequestParam Integer idPedido,
            @RequestParam Integer idProduto) {

        pedidoService.excluirItemOuPedidoInteiro(idPedido, idProduto);

        return "redirect:/pedidos";
    }

    //Realiza a exclusão completa de um pedido.
    @PostMapping("/excluir")
    public String excluirPedido(
            @RequestParam Integer idPedido,
            RedirectAttributes redirectAttributes) {

        pedidoService.excluirPedidoInteiro(idPedido);

        redirectAttributes.addFlashAttribute("sucesso", "Pedido excluído com sucesso!");

        return "redirect:/pedidos";
    }

    //Realiza a busca de produtos para auxiliar no cadastro e edição dos pedidos.
    @GetMapping("/produtos/buscar")
    public String buscarProdutos(
            @RequestParam String nomeProduto,
            Model model) {

        List<Produto> produtosEncontrados = produtoService.buscarPorNomeParcial(nomeProduto);

        carregarDadosTelaPedidos(model);
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("produtosEncontrados", produtosEncontrados);

        return "pedido/pedidos";
    }

    //Exibe apenas os pedidos vinculados a um cliente específico.
    @GetMapping("/cliente/{idCliente}")
    public String buscarPedidosPorCliente(
            @PathVariable Integer idCliente,
            Model model) {

        List<Pedido> pedidos = pedidoService.buscarPorCliente(idCliente);

        carregarDadosTelaPedidos(model);
        model.addAttribute("pedidos", pedidos);

        return "pedido/pedidos";
    }

    //Retorna para a tela inicial do sistema.
    @GetMapping("/inicio")
    public String voltarInicio() {
        return "redirect:/inicio";
    }

    //Carrega os dados auxiliares necessários para o funcionamento da tela de pedidos.
    private void carregarDadosTelaPedidos(Model model) {
        List<Cliente> clientes = clienteRepository.findAll();
        List<Produto> produtos = produtoService.listarTodos();
        List<FormaPagamento> formasPagamento = formaPagamentoRepository.findAll();
        List<StatusPedido> statusPedidos = statusPedidoRepository.findAllByOrderByIdStatusAsc();

        model.addAttribute("clientes", clientes);
        model.addAttribute("produtos", produtos);
        model.addAttribute("formasPagamento", formasPagamento);
        model.addAttribute("statusPedidos", statusPedidos);
    }

    //Recebe os produtos e quantidades informados e monta a estrutura utilizada para registrar os itens do pedido.
    private Map<Integer, Integer> montarMapaItens(
            List<Integer> produtos,
            List<Integer> quantidades) {

        if (produtos == null || quantidades == null || produtos.size() != quantidades.size()) {
            throw new IllegalArgumentException("Itens do pedido inválidos.");
        }

        Map<Integer, Integer> itens = new HashMap<>();

        for (int i = 0; i < produtos.size(); i++) {
            Integer idProduto = produtos.get(i);
            Integer quantidade = quantidades.get(i);

            if (idProduto != null && quantidade != null && quantidade > 0) {
                itens.merge(idProduto, quantidade, Integer::sum);
            }
        }

        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione ao menos um item ao pedido.");
        }

        return itens;
    }

    //Avança o pedido para o próximo status do fluxo de atendimento.
    @PostMapping("/status/avancar")
    public String avancarStatus(@RequestParam Integer idPedido) {
        pedidoService.avancarStatus(idPedido);
        return "redirect:/pedidos";
    }
}