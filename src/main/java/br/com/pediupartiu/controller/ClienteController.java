package br.com.pediupartiu.controller;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.Contato;
import br.com.pediupartiu.service.ClienteService;

//É nesta classe que todas as operações relacionadas aos clientes são controladas, como cadastro, edição, busca e exclusão.
@Controller
@RequestMapping("/clientes")
public class ClienteController {

	private final ClienteService clienteService;

	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	//Apresenta a tela de clientes e realiza a busca quando um filtro é informado.
	@GetMapping
	public String telaClientes(@RequestParam(required = false) String busca, Model model) {

		List<Cliente> clientes;

		if (busca != null && !busca.trim().isEmpty()) {
			clientes = clienteService.buscarClientes(busca);
		} else {
			clientes = clienteService.listarClientes();
		}

		model.addAttribute("clientes", clientes);
		model.addAttribute("busca", busca);

		return "cliente/clientes";
	}

	//Busca os dados do cliente selecionado para preencher o formulário de edição.
	@GetMapping("/editar/{idCliente}")
	public String editarCliente(@PathVariable Integer idCliente, Model model) {
		Cliente cliente = clienteService.buscarClientePorId(idCliente);
		List<Contato> contatos = clienteService.listarContatosPorCliente(idCliente);

		model.addAttribute("clientes", clienteService.listarClientes());
		model.addAttribute("clienteEdicao", cliente);
		model.addAttribute("contatosEdicao", contatos);

		return "cliente/clientes";
	}

	//Recebe os dados informados no cadastro e realiza a criação de um novo cliente.
	@PostMapping("/salvar")
	public String salvarCliente(
	        @RequestParam String nomeCliente,
	        @RequestParam String telefones,
	        @RequestParam(required = false) Boolean voltarPedido,
	        @RequestParam(required = false) String origemPedido,
	        @RequestParam(required = false) Integer idPedido,
	        RedirectAttributes redirectAttributes) {
		

		List<String> listaTelefones = Arrays.stream(telefones.split(",")).map(String::trim).filter(t -> !t.isEmpty())
				.toList();

		clienteService.cadastrarClienteComContatos(nomeCliente, listaTelefones);

		redirectAttributes.addFlashAttribute("sucesso", "Cliente cadastrado com sucesso!");

		if (Boolean.TRUE.equals(voltarPedido)) {

		    if ("editar".equals(origemPedido) && idPedido != null) {
		        return "redirect:/pedidos?abrirEditarPedido=" + idPedido;
		    }

		    return "redirect:/pedidos?abrirNovoPedido=true";
		}

		return "redirect:/clientes";
	}

	//Atualiza os dados de um cliente já existente.
	@PostMapping("/atualizar")
	public String atualizarCliente(
	        @RequestParam Integer idCliente,
	        @RequestParam String nomeCliente,
	        @RequestParam(required = false) List<String> telefones,
	        Model model,
	        RedirectAttributes redirectAttributes) {

		try {
			clienteService.atualizarClienteComTelefones(idCliente, nomeCliente, telefones);

			redirectAttributes.addFlashAttribute("sucesso", "Cliente atualizado com sucesso!");

			return "redirect:/clientes";

		} catch (IllegalArgumentException e) {
			model.addAttribute("clientes", clienteService.listarClientes());
			model.addAttribute("erro", e.getMessage());
			return "cliente/clientes";
		}
	}

	//Adiciona um novo telefone ao cliente selecionado.
	@PostMapping("/contato/adicionar")
	public String adicionarContato(@RequestParam Integer idCliente, @RequestParam String telefone) {

		clienteService.adicionarContato(idCliente, telefone);

		return "redirect:/clientes/editar/" + idCliente;
	}

	//Atualiza um telefone já cadastrado para o cliente.
	@PostMapping("/contato/atualizar")
	public String atualizarContato(@RequestParam Integer idCliente, @RequestParam Integer idContato,
			@RequestParam String telefone) {

		clienteService.atualizarTelefone(idContato, telefone);

		return "redirect:/clientes/editar/" + idCliente;
	}

	//Remove um telefone do cadastro do cliente.
	@PostMapping("/contato/excluir")
	public String excluirContato(@RequestParam Integer idCliente, @RequestParam Integer idContato) {

		clienteService.excluirContato(idContato);

		return "redirect:/clientes/editar/" + idCliente;
	}

	//Realiza a exclusão completa do cliente quando não existem pedidos vinculados.
	@PostMapping("/excluir")
	public String excluirCliente(
	        @RequestParam Integer idCliente,
	        Model model,
	        RedirectAttributes redirectAttributes) {

	    if (clienteService.clientePossuiPedidos(idCliente)) {

	        model.addAttribute(
	            "erro",
	            "Este cliente possui pedidos vinculados. Exclua os pedidos antes de excluir o cliente."
	        );

	        model.addAttribute("clientes", clienteService.listarClientes());

	        return "cliente/clientes";
	    }

	    clienteService.excluirCliente(idCliente);

	    redirectAttributes.addFlashAttribute("sucesso", "Cliente excluído com sucesso!");

	    return "redirect:/clientes";
	}

	//Retorna para a tela principal de clientes.
	@GetMapping("/voltar")
	public String voltarClientes() {
		return "redirect:/clientes";
	}

	//Retorna para a tela inicial do sistema.
	@GetMapping("/inicio")
	public String voltarInicio() {
		return "redirect:/inicio";
	}
}