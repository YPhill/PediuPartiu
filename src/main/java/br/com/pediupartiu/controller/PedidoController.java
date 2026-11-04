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
import org.springframework.web.client.RestClientException;
import br.com.pediupartiu.integracao.ComunicacaoApiClient;
import br.com.pediupartiu.integracao.FidelidadeApiClient;
import java.math.BigDecimal;

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
	private final ComunicacaoApiClient comunicacaoApiClient;
	private final FidelidadeApiClient fidelidadeApiClient;

	public PedidoController(PedidoService pedidoService, ItemPedidoService itemPedidoService,
			ProdutoService produtoService, ClienteRepository clienteRepository,
			FormaPagamentoRepository formaPagamentoRepository, StatusPedidoRepository statusPedidoRepository,
			ComunicacaoApiClient comunicacaoApiClient, FidelidadeApiClient fidelidadeApiClient) {

		this.pedidoService = pedidoService;
		this.itemPedidoService = itemPedidoService;
		this.produtoService = produtoService;
		this.clienteRepository = clienteRepository;
		this.formaPagamentoRepository = formaPagamentoRepository;
		this.statusPedidoRepository = statusPedidoRepository;
		this.comunicacaoApiClient = comunicacaoApiClient;
		this.fidelidadeApiClient = fidelidadeApiClient;
	}

	// Apresenta a tela de pedidos e permite realizar buscas utilizando os filtros
	// disponíveis.
	@GetMapping
	public String telaPedidos(@RequestParam(required = false) String busca,
			@RequestParam(required = false) String status, @RequestParam(required = false) String data, Model model) {

		carregarDadosTelaPedidos(model);

		List<Pedido> pedidos = pedidoService.buscarPedidos(busca, status, data);

		carregarDescontosPedidos(pedidos);

		model.addAttribute("pedidos", pedidos);
		model.addAttribute("busca", busca);
		model.addAttribute("status", status);
		model.addAttribute("data", data);

		return "pedido/pedidos";
	}

	// Busca os dados de um pedido já existente para preenchimento da tela de
	// edição.
	@GetMapping("/editar/{idPedido}")
	public String editarPedido(@PathVariable Integer idPedido, Model model) {
		Pedido pedido = pedidoService.buscarPorId(idPedido);
		List<ItemPedido> itens = itemPedidoService.buscarItensPorPedido(idPedido);

		carregarDadosTelaPedidos(model);
		List<Pedido> pedidos = pedidoService.listarTodos();
		carregarDescontosPedidos(pedidos);
		model.addAttribute("pedidos", pedidos);
		model.addAttribute("pedidoEdicao", pedido);
		model.addAttribute("itensEdicao", itens);
		model.addAttribute("totalPedido", itemPedidoService.calcularTotalPedido(idPedido));

		return "pedido/pedidos";
	}

	// Recebe os dados informados no cadastro e realiza a criação de um novo pedido.
	@PostMapping("/salvar")
	public String salvarPedido(@RequestParam Integer idCliente, @RequestParam Integer idPagamento,
			@RequestParam(required = false) String observacao, @RequestParam List<Integer> produtos,
			@RequestParam List<Integer> quantidades,
			@RequestParam(required = false, defaultValue = "false") Boolean resgatarBrinde,
			RedirectAttributes redirectAttributes) {

		Map<Integer, Integer> itens = montarMapaItens(produtos, quantidades);

		// identifica o desconto do brinde
		BigDecimal valorDesconto = BigDecimal.ZERO;
		Integer idProdutoBrinde = null;

		// valida os itens necessários para o resgate
		if (Boolean.TRUE.equals(resgatarBrinde)) {

			// verifica se o cliente possui brinde disponível
			Map<String, Object> progresso = fidelidadeApiClient.consultarProgressoCliente(idCliente);

			if (progresso == null || !Boolean.TRUE.equals(progresso.get("brindePendente"))) {
				throw new IllegalArgumentException("Este cliente não possui brinde disponível.");
			}

			idProdutoBrinde = fidelidadeApiClient.consultarProdutoBrinde();

			if (idProdutoBrinde == null) {
				throw new IllegalArgumentException("Nenhum produto foi configurado como brinde.");
			}

			Integer quantidadeBrinde = itens.getOrDefault(idProdutoBrinde, 0);

			if (quantidadeBrinde == 0) {
				throw new IllegalArgumentException("Adicione o produto do brinde ao pedido.");
			}

			// verifica se existe outro item pago
			final Integer produtoBrindeSelecionado = idProdutoBrinde;

			boolean possuiItemPago = quantidadeBrinde > 1
					|| itens.entrySet().stream().anyMatch(item -> !item.getKey().equals(produtoBrindeSelecionado));

			if (!possuiItemPago) {
				throw new IllegalArgumentException("O pedido precisa conter pelo menos um item pago.");
			}
			// obtém o preço do produto configurado como brinde
			Produto produtoBrinde = produtoService.buscarPorId(idProdutoBrinde);

			valorDesconto = BigDecimal.valueOf(produtoBrinde.getPrecoUnitario());
		}

		Pedido pedido;

		if (Boolean.TRUE.equals(resgatarBrinde)) {
			pedido = pedidoService.criarPedidoComDesconto(idCliente, idPagamento, observacao, itens, valorDesconto);
		} else {
			pedido = pedidoService.criarPedidoCompleto(idCliente, idPagamento, observacao, itens);
		}

		// confirma o resgate no microserviço
		if (Boolean.TRUE.equals(resgatarBrinde)) {
			try {
				fidelidadeApiClient.resgatarBrinde(pedido.getIdPedido(), idCliente, idProdutoBrinde, valorDesconto);

			} catch (RestClientException e) {

				// verifica se o resgate foi registrado apesar da falha
				boolean resgateConfirmado = false;

				try {
					resgateConfirmado = fidelidadeApiClient.consultarResgatePedido(pedido.getIdPedido());

				} catch (RestClientException consultaErro) {
					// mantém o pedido para conferência posterior
				}

				if (!resgateConfirmado) {
					redirectAttributes.addFlashAttribute("mensagemAviso",
							"O pedido #" + pedido.getIdPedido() + " foi salvo com desconto, mas o resgate não pôde "
									+ "ser confirmado. Verifique antes de tentar novamente.");

					return "redirect:/pedidos";
				}
			}
		}

		try {
			comunicacaoApiClient.notificarStatusPedido(pedido.getIdPedido(), pedido.getCliente().getIdCliente(),
					pedido.getStatusPedido().getIdStatus());
		} catch (RestClientException e) {
			// O pedido foi salvo, mesmo que a notificação falhe.
		}

		redirectAttributes.addFlashAttribute("sucesso", "Pedido cadastrado com sucesso!");
		return "redirect:/pedidos";
	}

	// Atualiza as informações de um pedido já cadastrado.
	@PostMapping("/atualizar")
	public String atualizarPedido(@RequestParam Integer idPedido, @RequestParam(required = false) Integer idCliente,
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

		Pedido pedidoAntes = pedidoService.buscarPorId(idPedido);
		Integer idStatusAnterior = pedidoAntes.getStatusPedido().getIdStatus();
		

		// trata o cancelamento de pedidos com brinde
		if (fidelidadeApiClient.consultarResgatePedido(idPedido)) {

		    if ("Cancelado".equalsIgnoreCase(nomeStatus)) {

		        return cancelarPedidoComBrinde(
		                idPedido, redirectAttributes);
		    }

		    // impede outras alterações no pedido com brinde
		    redirectAttributes.addFlashAttribute(
		            "mensagemAviso",
		            "Este pedido possui um brinde resgatado e não pode ser editado."
		    );

		    return "redirect:/pedidos";
		}

		Pedido pedidoAtualizado = pedidoService.editarPedidoCompleto(idPedido, idCliente, nomeStatus,
				nomeFormaPagamento, observacao, itens, Boolean.TRUE.equals(atualizarDataHoraPedido),
				Boolean.TRUE.equals(voltarStatusPendente));

		Integer idStatusNovo = pedidoAtualizado.getStatusPedido().getIdStatus();
		
		// estorna a estrela quando o pedido deixa de ser entregue
		if (idStatusAnterior.equals(8) && !idStatusNovo.equals(8)) {
		    fidelidadeApiClient.estornarPedido(idPedido);
		}

		// contabiliza a estrela quando o pedido volta a ser entregue
		if (!idStatusAnterior.equals(8) && idStatusNovo.equals(8)) {
		    fidelidadeApiClient.notificarPedidoEntregue(
		            idPedido,
		            pedidoAtualizado.getCliente().getIdCliente());
		}

		if (!idStatusAnterior.equals(idStatusNovo)) {
			try {
				comunicacaoApiClient.notificarStatusPedido(pedidoAtualizado.getIdPedido(),
						pedidoAtualizado.getCliente().getIdCliente(), idStatusNovo);
			} catch (RestClientException e) {
				redirectAttributes.addFlashAttribute("mensagemAviso",
						"Pedido atualizado, mas não foi possível enviar a notificação.");
			}
		}

		redirectAttributes.addFlashAttribute("sucesso", "Pedido atualizado com sucesso!");
		return "redirect:/pedidos";
	}

	// Atualiza a quantidade de um item já existente no pedido.
	@PostMapping("/item/atualizar")
	public String atualizarItem(@RequestParam Integer idPedido, @RequestParam Integer idProduto,
			@RequestParam Integer quantidade) {

		pedidoService.atualizarQuantidadeItem(idPedido, idProduto, quantidade);

		return "redirect:/pedidos/editar/" + idPedido;
	}

	// protege a fidelidade na exclusão de itens
	@PostMapping("/item/excluir")
	public String excluirItemOuPedido(@RequestParam Integer idPedido,
	        @RequestParam Integer idProduto,
	        RedirectAttributes redirectAttributes) {

	    Pedido pedido = pedidoService.buscarPorId(idPedido);

	    // impede alterações em pedidos com brinde resgatado
	    if (fidelidadeApiClient.consultarResgatePedido(idPedido)) {
	        redirectAttributes.addFlashAttribute("mensagemAviso",
	                "Este pedido possui um brinde resgatado e não pode ter itens excluídos.");
	        return "redirect:/pedidos";
	    }

	    // verifica se a exclusão removerá o pedido inteiro
	    if (itemPedidoService.contarItens(idPedido) <= 1
	            && "Entregue".equalsIgnoreCase(pedido.getStatusPedido().getNomeStatus())) {

	        fidelidadeApiClient.estornarPedido(idPedido);
	    }

	    pedidoService.excluirItemOuPedidoInteiro(idPedido, idProduto);

	    return "redirect:/pedidos";
	}

	// exclui o pedido e atualiza a fidelidade
	@PostMapping("/excluir")
	public String excluirPedido(@RequestParam Integer idPedido, RedirectAttributes redirectAttributes) {

	    Pedido pedido = pedidoService.buscarPorId(idPedido);

	    // impede excluir um pedido com brinde resgatado
	    if (fidelidadeApiClient.consultarResgatePedido(idPedido)) {
	        redirectAttributes.addFlashAttribute("mensagemAviso",
	                "Este pedido possui um brinde resgatado e não pode ser excluído.");

	        return "redirect:/pedidos";
	    }

	    // estorna a estrela antes de excluir o pedido
	    if ("Entregue".equalsIgnoreCase(pedido.getStatusPedido().getNomeStatus())) {
	        fidelidadeApiClient.estornarPedido(idPedido);
	    }

	    pedidoService.excluirPedidoInteiro(idPedido);

	    redirectAttributes.addFlashAttribute("sucesso", "Pedido excluído com sucesso!");

	    return "redirect:/pedidos";
	}

	// Realiza a busca de produtos para auxiliar no cadastro e edição dos pedidos.
	@GetMapping("/produtos/buscar")
	public String buscarProdutos(@RequestParam String nomeProduto, Model model) {

		List<Produto> produtosEncontrados = produtoService.buscarPorNomeParcial(nomeProduto);

		carregarDadosTelaPedidos(model);
		List<Pedido> pedidos = pedidoService.listarTodos();
		carregarDescontosPedidos(pedidos);
		model.addAttribute("pedidos", pedidos);
		model.addAttribute("produtosEncontrados", produtosEncontrados);

		return "pedido/pedidos";
	}

	// Exibe apenas os pedidos vinculados a um cliente específico.
	@GetMapping("/cliente/{idCliente}")
	public String buscarPedidosPorCliente(@PathVariable Integer idCliente, Model model) {

		List<Pedido> pedidos = pedidoService.buscarPorCliente(idCliente);

		carregarDadosTelaPedidos(model);
		carregarDescontosPedidos(pedidos);
		model.addAttribute("pedidos", pedidos);

		return "pedido/pedidos";
	}

	// Retorna para a tela inicial do sistema.
	@GetMapping("/inicio")
	public String voltarInicio() {
		return "redirect:/inicio";
	}
	
	
	// carrega os descontos registrados na fidelidade
	private void carregarDescontosPedidos(List<Pedido> pedidos) {

	    for (Pedido pedido : pedidos) {
	        try {
	            if (fidelidadeApiClient.consultarResgatePedido(pedido.getIdPedido())) {
	                BigDecimal desconto = fidelidadeApiClient.consultarDescontoPedido(pedido.getIdPedido());

	                pedido.setValorDesconto(desconto == null ? BigDecimal.ZERO : desconto);
	            }
	        } catch (RestClientException e) {
	            // mantém a tela disponível se a fidelidade falhar
	        }
	    }
	}
	

	// Carrega os dados auxiliares necessários para o funcionamento da tela de
	// pedidos.
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

	// Recebe os produtos e quantidades informados e monta a estrutura utilizada
	// para registrar os itens do pedido.
	private Map<Integer, Integer> montarMapaItens(List<Integer> produtos, List<Integer> quantidades) {

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

	// Avança o pedido para o próximo status do fluxo de atendimento.

	@PostMapping("/status/avancar")
	public String avancarStatus(@RequestParam Integer idPedido, RedirectAttributes redirectAttributes) {

		Pedido pedido = pedidoService.avancarStatus(idPedido);

		Integer idCliente = pedido.getCliente().getIdCliente();
		Integer idStatus = pedido.getStatusPedido().getIdStatus();

		// Notifica a Fidelidade somente quando o pedido é entregue.
		if (idStatus.equals(8)) {
			try {
				fidelidadeApiClient.notificarPedidoEntregue(pedido.getIdPedido(), idCliente);
			} catch (RestClientException e) {
				// A falha da Fidelidade não impede o funcionamento do pedido.
			}
		}

		try {
			comunicacaoApiClient.notificarStatusPedido(pedido.getIdPedido(), idCliente, idStatus);

			redirectAttributes.addFlashAttribute("mensagemSucesso", "Status atualizado e notificação enviada.");

		} catch (RestClientException e) {
			redirectAttributes.addFlashAttribute("mensagemAviso",
					"Status atualizado, mas não foi possível enviar a notificação.");
		}

		return "redirect:/pedidos";
	}
	
	// cancela o pedido e devolve o direito ao brinde
	@PostMapping("/cancelar-brinde")
	public String cancelarPedidoComBrinde(
	        @RequestParam Integer idPedido,
	        RedirectAttributes redirectAttributes) {

	    try {
	        Pedido pedido = pedidoService.buscarPorId(idPedido);

	        // confirma a existência do resgate
	        if (!fidelidadeApiClient.consultarResgatePedido(idPedido)) {
	            throw new IllegalStateException(
	                    "Este pedido não possui brinde resgatado.");
	        }

	        // verifica se a devolução já foi realizada
	        if (fidelidadeApiClient.consultarBrindeDevolvido(idPedido)) {
	            throw new IllegalStateException(
	                    "O brinde deste pedido já foi devolvido.");
	        }

	        boolean cancelado = "Cancelado".equalsIgnoreCase(
	                pedido.getStatusPedido().getNomeStatus());

	        // valida o progresso antes de cancelar
	        if (!cancelado) {
	            Map<String, Object> progresso =
	                    fidelidadeApiClient.consultarProgressoCliente(
	                            pedido.getCliente().getIdCliente());

	            if (progresso == null
	                    || !(progresso.get("estrelas") instanceof Number)
	                    || !(progresso.get("estrelasPendentesEstorno") instanceof Number)
	                    || ((Number) progresso.get("estrelas")).intValue() != 0
	                    || ((Number) progresso.get("estrelasPendentesEstorno")).intValue() != 0
	                    || !Boolean.FALSE.equals(progresso.get("brindePendente"))) {

	                throw new IllegalStateException(
	                        "O progresso do cliente não permite devolver o brinde.");
	            }

	            // salva o cancelamento no monólito
	            pedidoService.cancelarPedidoComBrinde(idPedido);
	        }

	        // permite concluir uma devolução pendente
	        fidelidadeApiClient.devolverBrinde(idPedido);

	        redirectAttributes.addFlashAttribute(
	                "sucesso",
	                "Pedido cancelado e brinde devolvido com sucesso.");

	    } catch (Exception e) {

	        redirectAttributes.addFlashAttribute(
	                "mensagemAviso",
	                "Cancelamento não concluído: " + e.getMessage()
	                + " Verifique o estado do pedido antes de tentar novamente.");
	    }

	    return "redirect:/pedidos";
	}

}