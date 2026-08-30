package br.com.pediupartiu.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.Contato;
import br.com.pediupartiu.model.Pedido;
import br.com.pediupartiu.repository.ClienteRepository;
import br.com.pediupartiu.repository.ContatoRepository;
import br.com.pediupartiu.repository.PedidoRepository;

//É nesta classe que ficam as regras de negócio relacionadas aos clientes.
@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            ContatoRepository contatoRepository,
            PedidoRepository pedidoRepository) {

        this.clienteRepository = clienteRepository;
        this.contatoRepository = contatoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    //Retorna todos os clientes cadastrados no sistema.
    public List<Cliente> listarClientes() {

        List<Cliente> clientes = clienteRepository.findAll();

        for (Cliente cliente : clientes) {

        	List<Pedido> pedidos = pedidoRepository.findByClienteOrderByDataHoraPedidoDesc(cliente);

            cliente.setPossuiPedidos(!pedidos.isEmpty());
        }

        return clientes;
    }
    

    public List<Contato> listarContatosPorCliente(Integer idCliente) {
        Cliente cliente = buscarClientePorId(idCliente);
        return contatoRepository.findByCliente(cliente);
    }

    public Cliente buscarClientePorId(Integer idCliente) {
        validarId(idCliente, "ID do cliente inválido.");

        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
    }

    public Contato buscarContatoPorId(Integer idContato) {
        validarId(idContato, "ID do contato inválido.");

        return contatoRepository.findById(idContato)
                .orElseThrow(() -> new IllegalArgumentException("Contato não encontrado."));
    }

    //Realiza o cadastro de um cliente juntamente com seus contatos.
    @Transactional
    public Cliente cadastrarClienteComContatos(String nomeCliente, List<String> telefones) {
        validarNomeCliente(nomeCliente);
        validarListaTelefones(telefones);

        Cliente cliente = new Cliente();
        cliente.setNomeCliente(nomeCliente.trim());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        for (String telefone : telefones) {
            validarTelefone(telefone);

            Contato contato = new Contato();
            contato.setTelefone(telefone.trim());
            contato.setCliente(clienteSalvo);

            contatoRepository.save(contato);
        }

        return clienteSalvo;
    }

    public Cliente atualizarNomeCliente(Integer idCliente, String novoNome) {
        validarNomeCliente(novoNome);

        Cliente cliente = buscarClientePorId(idCliente);
        cliente.setNomeCliente(novoNome.trim());

        return clienteRepository.save(cliente);
    }

    //Adiciona um novo telefone para o cliente informado.
    @Transactional
    public Contato adicionarContato(Integer idCliente, String telefone) {
        validarTelefone(telefone);

        Cliente cliente = buscarClientePorId(idCliente);

        Contato contato = new Contato();
        contato.setTelefone(telefone.trim());
        contato.setCliente(cliente);

        return contatoRepository.save(contato);
    }

    //Atualiza um telefone já cadastrado.
    @Transactional
    public Contato atualizarTelefone(Integer idContato, String novoTelefone) {
        validarTelefone(novoTelefone);

        Contato contato = buscarContatoPorId(idContato);
        contato.setTelefone(novoTelefone.trim());

        return contatoRepository.save(contato);
    }

    //Realiza a exclusão de um cliente.
    @Transactional
    public void excluirCliente(Integer idCliente) {
        Cliente cliente = buscarClientePorId(idCliente);

        clienteRepository.delete(cliente);
    }

    //Realiza a exclusão de um contato.
    @Transactional
    public void excluirContato(Integer idContato) {
        Contato contato = buscarContatoPorId(idContato);
        Cliente cliente = contato.getCliente();

        List<Contato> contatosDoCliente = contatoRepository.findByCliente(cliente);

        if (contatosDoCliente.size() <= 1) {
            clienteRepository.delete(cliente);
        } else {
            contatoRepository.delete(contato);
        }
    }

    //Valida os dados informados para o nome do cliente.
    private void validarNomeCliente(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do cliente.");
        }

        if (nomeCliente.trim().length() > 100) {
            throw new IllegalArgumentException("O nome do cliente deve ter no máximo 100 caracteres.");
        }
    }

    //Valida a lista de telefones informada.
    private void validarListaTelefones(List<String> telefones) {
        if (telefones == null || telefones.isEmpty()) {
            throw new IllegalArgumentException("Adicione ao menos um telefone.");
        }

        for (String telefone : telefones) {
            validarTelefone(telefone);
        }
    }

    //Valida um telefone antes de realizar seu cadastro.
    private void validarTelefone(String telefone) {
        if (telefone == null || telefone.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o telefone.");
        }

        String telefoneTratado = telefone.trim();

        if (telefoneTratado.length() < 8) {
            throw new IllegalArgumentException("O telefone deve ter pelo menos 8 caracteres.");
        }

        if (telefoneTratado.length() > 25) {
            throw new IllegalArgumentException("O telefone deve ter no máximo 25 caracteres.");
        }

        if (!telefoneTratado.matches("^[0-9+()\\- ]+$")) {
            throw new IllegalArgumentException("Telefone inválido. Use apenas números, +, (, ), - e espaço.");
        }
    }

    //Valida se o identificador informado é válido.
    private void validarId(Integer id, String mensagemErro) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(mensagemErro);
        }
    }
    
    //Atualiza os dados e telefones de um cliente já cadastrado.
    @Transactional
    public Cliente atualizarClienteComTelefones(Integer idCliente, String novoNome, List<String> telefones) {
        validarNomeCliente(novoNome);

        List<String> telefonesTratados = telefones == null ? List.of() :
                telefones.stream()
                        .map(String::trim)
                        .filter(t -> !t.isEmpty())
                        .toList();

        validarListaTelefones(telefonesTratados);

        Cliente cliente = buscarClientePorId(idCliente);
        cliente.setNomeCliente(novoNome.trim());

        List<Contato> contatosAtuais = contatoRepository.findByCliente(cliente);

        for (int i = 0; i < telefonesTratados.size(); i++) {
            String telefone = telefonesTratados.get(i);

            if (i < contatosAtuais.size()) {
                Contato contatoExistente = contatosAtuais.get(i);
                contatoExistente.setTelefone(telefone);
                contatoRepository.save(contatoExistente);
            } else {
                Contato novoContato = new Contato();
                novoContato.setTelefone(telefone);
                novoContato.setCliente(cliente);
                contatoRepository.save(novoContato);
            }
        }

        if (contatosAtuais.size() > telefonesTratados.size()) {
            for (int i = telefonesTratados.size(); i < contatosAtuais.size(); i++) {
                contatoRepository.delete(contatosAtuais.get(i));
            }
        }

        return clienteRepository.save(cliente);
    }
    
    //Realiza a busca de clientes utilizando o nome ou telefone informado.
    public List<Cliente> buscarClientes(String busca) {
        String termo = busca == null ? "" : busca.trim();

        if (termo.isEmpty()) {
            return listarClientes();
        }

        return clienteRepository.buscarPorNomeOuTelefone(termo);
    }
    
    //Verifica se o cliente possui pedidos vinculados.
    public boolean clientePossuiPedidos(Integer idCliente) {

        Cliente cliente = buscarClientePorId(idCliente);

        List<Pedido> pedidos = pedidoRepository.findByClienteOrderByDataHoraPedidoDesc(cliente);

        return pedidos != null && !pedidos.isEmpty();
    }
    
}