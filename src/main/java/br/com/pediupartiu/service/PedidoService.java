package br.com.pediupartiu.service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.pediupartiu.model.Cliente;
import br.com.pediupartiu.model.FormaPagamento;
import br.com.pediupartiu.model.ItemPedido;
import br.com.pediupartiu.model.Pedido;
import br.com.pediupartiu.model.StatusPedido;
import br.com.pediupartiu.repository.ClienteRepository;
import br.com.pediupartiu.repository.FormaPagamentoRepository;
import br.com.pediupartiu.repository.PedidoRepository;
import br.com.pediupartiu.repository.StatusPedidoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;

//É nesta classe que ficam as regras de negócio relacionadas aos pedidos.
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final StatusPedidoRepository statusPedidoRepository;
    private final ItemPedidoService itemPedidoService;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            FormaPagamentoRepository formaPagamentoRepository,
            StatusPedidoRepository statusPedidoRepository,
            ItemPedidoService itemPedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.statusPedidoRepository = statusPedidoRepository;
        this.itemPedidoService = itemPedidoService;
    }

    //Retorna todos os pedidos cadastrados no sistema.
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll(
                org.springframework.data.domain.Sort.by(
                        org.springframework.data.domain.Sort.Direction.DESC,
                        "dataHoraPedido"
                )
        );
    }

    public Pedido buscarPorId(Integer idPedido) {
        validarId(idPedido, "ID do pedido inválido.");

        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));
    }

    //Realiza a busca dos pedidos vinculados a um cliente específico.
    public List<Pedido> buscarPorCliente(Integer idCliente) {
        Cliente cliente = buscarClientePorId(idCliente);
        return pedidoRepository.findByClienteOrderByDataHoraPedidoDesc(cliente);
    }

    //Retorna os itens pertencentes a um pedido.
    public List<ItemPedido> buscarItensDoPedido(Integer idPedido) {
        return itemPedidoService.buscarItensPorPedido(idPedido);
    }

    //Calcula o valor total de um pedido.
    public Double calcularTotalPedido(Integer idPedido) {
        return itemPedidoService.calcularTotalPedido(idPedido);
    }

    //Realiza o cadastro completo de um pedido juntamente com seus itens.
    @Transactional
    public Pedido criarPedidoCompleto(
            Integer idCliente,
            Integer idPagamento,
            String observacao,
            Map<Integer, Integer> itens) {

        validarItensPedido(itens);

        Cliente cliente = buscarClientePorId(idCliente);
        FormaPagamento formaPagamento = buscarFormaPagamentoPorId(idPagamento);
        StatusPedido statusInicial = buscarStatusPorNome("Pendente");

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFormaPagamento(formaPagamento);
        pedido.setStatusPedido(statusInicial);
        pedido.setObservacao(tratarTextoOpcional(observacao));

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        for (Map.Entry<Integer, Integer> item : itens.entrySet()) {
            itemPedidoService.adicionarItemAoPedido(
                    pedidoSalvo.getIdPedido(),
                    item.getKey(),
                    item.getValue()
            );
        }

        return pedidoSalvo;
    }

    //Atualiza as informações básicas de um pedido.
    @Transactional
    public Pedido editarPedido(
            Integer idPedido,
            String nomeStatus,
            String nomeFormaPagamento,
            String observacao) {

        Pedido pedido = buscarPorId(idPedido);

        if (nomeStatus != null && !nomeStatus.trim().isEmpty()) {
            StatusPedido status = statusPedidoRepository
                    .findByNomeStatusIgnoreCase(nomeStatus.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Status não encontrado."));
            pedido.setStatusPedido(status);
        }

        if (nomeFormaPagamento != null && !nomeFormaPagamento.trim().isEmpty()) {
            FormaPagamento formaPagamento = formaPagamentoRepository
                    .findByNomeFormaPagamentoIgnoreCase(nomeFormaPagamento.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Forma de pagamento não encontrada."));
            pedido.setFormaPagamento(formaPagamento);
        }

        pedido.setObservacao(tratarTextoOpcional(observacao));

        return pedidoRepository.save(pedido);
    }

    //Realiza a edição completa de um pedido, incluindo cliente, itens, status e pagamento.
    @Transactional
    public Pedido editarPedidoCompleto(
            Integer idPedido,
            Integer idCliente,
            String nomeStatus,
            String nomeFormaPagamento,
            String observacao,
            Map<Integer, Integer> itensTela,
            Boolean atualizarDataHoraPedido,
            Boolean voltarStatusPendente) {

        validarId(idPedido, "ID do pedido inválido.");

        Pedido pedido = buscarPorId(idPedido);

        if (idCliente != null) {
            Cliente cliente = buscarClientePorId(idCliente);
            pedido.setCliente(cliente);
        }

        if (nomeStatus != null && !nomeStatus.trim().isEmpty()) {
            StatusPedido status = statusPedidoRepository
                    .findByNomeStatusIgnoreCase(nomeStatus.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Status não encontrado."));
            pedido.setStatusPedido(status);
        }
        
        if (Boolean.TRUE.equals(voltarStatusPendente)) {
            pedido.setStatusPedido(buscarStatusPorNome("Pendente"));
        }

        if (nomeFormaPagamento != null && !nomeFormaPagamento.trim().isEmpty()) {
            FormaPagamento formaPagamento = formaPagamentoRepository
                    .findByNomeFormaPagamentoIgnoreCase(nomeFormaPagamento.trim())
                    .orElseThrow(() -> new IllegalArgumentException("Forma de pagamento não encontrada."));
            pedido.setFormaPagamento(formaPagamento);
        }

        pedido.setObservacao(tratarTextoOpcional(observacao));

        if (Boolean.TRUE.equals(atualizarDataHoraPedido)) {
            pedido.setDataHoraPedido(LocalDateTime.now());
        }

        atualizarItensDoPedido(idPedido, itensTela);

        return pedidoRepository.save(pedido);
    }

    //Atualiza os itens de um pedido de acordo com as informações informadas na tela.
    private void atualizarItensDoPedido(Integer idPedido, Map<Integer, Integer> itensTela) {
        validarItensPedido(itensTela);

        List<ItemPedido> itensAtuais = itemPedidoService.buscarItensPorPedido(idPedido);

        for (ItemPedido itemAtual : itensAtuais) {
            Integer idProdutoAtual = itemAtual.getProduto().getIdProduto();

            if (!itensTela.containsKey(idProdutoAtual)) {
                itemPedidoService.excluirItem(idPedido, idProdutoAtual);
            }
        }

        for (Map.Entry<Integer, Integer> itemTela : itensTela.entrySet()) {
            Integer idProdutoTela = itemTela.getKey();
            Integer quantidadeTela = itemTela.getValue();

            ItemPedido itemExistente = itensAtuais.stream()
                    .filter(item -> item.getProduto().getIdProduto().equals(idProdutoTela))
                    .findFirst()
                    .orElse(null);

            if (itemExistente == null) {
                itemPedidoService.adicionarItemAoPedido(
                        idPedido,
                        idProdutoTela,
                        quantidadeTela
                );
            } else if (!itemExistente.getQuantidade().equals(quantidadeTela)) {
                itemPedidoService.atualizarQuantidade(
                        idPedido,
                        idProdutoTela,
                        quantidadeTela
                );
            }
        }
    }

    //Realiza a exclusão completa de um pedido e de todos os seus itens.
    @Transactional
    public void excluirPedidoInteiro(Integer idPedido) {
        Pedido pedido = buscarPorId(idPedido);

        List<ItemPedido> itens = itemPedidoService.buscarItensPorPedido(idPedido);

        for (ItemPedido item : itens) {
            itemPedidoService.excluirItem(
                    idPedido,
                    item.getProduto().getIdProduto()
            );
        }

        pedidoRepository.delete(pedido);
    }

    //Remove um item específico ou exclui o pedido inteiro quando necessário.
    @Transactional
    public void excluirItemOuPedidoInteiro(Integer idPedido, Integer idProduto) {
        long quantidadeItens = itemPedidoService.contarItens(idPedido);

        if (quantidadeItens <= 1) {
            excluirPedidoInteiro(idPedido);
        } else {
            itemPedidoService.excluirItem(idPedido, idProduto);
        }
    }

    //Atualiza a quantidade de um item do pedido.
    @Transactional
    public ItemPedido atualizarQuantidadeItem(
            Integer idPedido,
            Integer idProduto,
            Integer novaQuantidade) {

        return itemPedidoService.atualizarQuantidade(idPedido, idProduto, novaQuantidade);
    }

    //Avança o pedido para o próximo status do fluxo de atendimento.
    @Transactional
    public Pedido avancarStatus(Integer idPedido) {
        Pedido pedido = buscarPorId(idPedido);

        String statusAtual = pedido.getStatusPedido().getNomeStatus();

        if ("Pendente".equalsIgnoreCase(statusAtual)) {
            pedido.setStatusPedido(buscarStatusPorNome("Preparando"));

        } else if ("Preparando".equalsIgnoreCase(statusAtual)) {
            pedido.setStatusPedido(buscarStatusPorNome("Pronto"));

        } else if ("Pronto".equalsIgnoreCase(statusAtual)) {
            pedido.setStatusPedido(buscarStatusPorNome("Entregue"));

        } else {
            throw new IllegalArgumentException("Este pedido não pode mais avançar de status.");
        }

        return pedidoRepository.save(pedido);
    }
    
    //Realiza a busca de pedidos utilizando os filtros informados.
    public List<Pedido> buscarPedidos(String busca, String status, String data) {
        String termoBusca = busca == null ? "" : busca.trim();
        String filtroStatus = status == null ? "" : status.trim();
        String filtroData = data == null ? "" : data.trim();

        if (termoBusca.isEmpty() && filtroStatus.isEmpty() && filtroData.isEmpty()) {
            return listarTodos();
        }

        return pedidoRepository.buscarPedidos(termoBusca, filtroStatus, filtroData);
    }

    private Cliente buscarClientePorId(Integer idCliente) {
        validarId(idCliente, "ID do cliente inválido.");

        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
    }

    private FormaPagamento buscarFormaPagamentoPorId(Integer idPagamento) {
        validarId(idPagamento, "ID da forma de pagamento inválido.");

        return formaPagamentoRepository.findById(idPagamento)
                .orElseThrow(() -> new IllegalArgumentException("Forma de pagamento não encontrada."));
    }

    private StatusPedido buscarStatusPorNome(String nomeStatus) {
        return statusPedidoRepository.findByNomeStatusIgnoreCase(nomeStatus)
                .orElseThrow(() -> new IllegalArgumentException("Status não encontrado."));
    }

    //Valida os itens informados para o pedido.
    private void validarItensPedido(Map<Integer, Integer> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione ao menos um item ao pedido.");
        }

        for (Map.Entry<Integer, Integer> item : itens.entrySet()) {
            validarId(item.getKey(), "ID do produto inválido.");

            if (item.getValue() == null || item.getValue() <= 0) {
                throw new IllegalArgumentException("A quantidade dos itens deve ser maior que zero.");
            }
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
    
    //Retorna a quantidade de pedidos realizados no dia atual.
    public long contarPedidosHoje() {
        LocalDate hoje = LocalDate.now();

        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.plusDays(1).atStartOfDay();

        return pedidoRepository.countByDataHoraPedidoBetween(inicioDoDia, fimDoDia);
    }
    
    //Retorna a quantidade de pedidos de um determinado status realizados hoje.
    public long contarPedidosHojePorStatus(String nomeStatus) {
        LocalDate hoje = LocalDate.now();

        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.plusDays(1).atStartOfDay();

        return pedidoRepository.countByDataHoraPedidoBetweenAndStatusPedido_NomeStatus(
                inicioDoDia,
                fimDoDia,
                nomeStatus
        );
    }
    
    //Calcula o faturamento obtido nos últimos sete dias.
    public double calcularFaturamentoUltimos7Dias() {
        LocalDateTime inicio = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();

        Double total = pedidoRepository.calcularFaturamentoEntre(inicio, fim);

        return total == null ? 0.0 : total;
    }

    //Calcula o ticket médio dos pedidos realizados nos últimos sete dias.
    public double calcularTicketMedioUltimos7Dias() {
        LocalDateTime inicio = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();

        long totalPedidos = pedidoRepository.countByDataHoraPedidoBetween(inicio, fim);

        if (totalPedidos == 0) {
            return 0.0;
        }

        return calcularFaturamentoUltimos7Dias() / totalPedidos;
    }

    //Retorna os dados utilizados no gráfico de itens mais pedidos do dashboard.
    public List<Map<String, Object>> buscarItensMaisPedidosDashboard() {
        LocalDateTime inicio = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();

        List<Object[]> resultados = itemPedidoService.buscarItensMaisPedidos(inicio, fim);

        return montarDadosDashboard(resultados);
    }

    //Retorna os dados utilizados no gráfico de formas de pagamento do dashboard.
    public List<Map<String, Object>> buscarFormasPagamentoDashboard() {
        LocalDateTime inicio = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime fim = LocalDate.now().plusDays(1).atStartOfDay();

        List<Object[]> resultados = pedidoRepository.buscarFormasPagamentoDashboard(inicio, fim);

        return montarDadosDashboard(resultados);
    }

    //Organiza os dados que serão apresentados nos gráficos do dashboard.
    private List<Map<String, Object>> montarDadosDashboard(List<Object[]> resultados) {
        List<Map<String, Object>> dados = new ArrayList<>();

        if (resultados == null || resultados.isEmpty()) {
            return dados;
        }

        double maiorValor = Number.class.cast(resultados.get(0)[1]).doubleValue();

        for (Object[] linha : resultados) {
            String nome = String.valueOf(linha[0]);
            double quantidade = Number.class.cast(linha[1]).doubleValue();

            Map<String, Object> item = Map.of(
                    "nome", nome,
                    "quantidade", quantidade,
                    "percentual", maiorValor == 0 ? 0 : (quantidade / maiorValor) * 100
            );

            dados.add(item);
        }

        return dados;
    }   
    
    //Calcula o faturamento obtido no dia atual.
    public double calcularFaturamentoHoje() {
        LocalDate hoje = LocalDate.now();

        LocalDateTime inicioDoDia = hoje.atStartOfDay();
        LocalDateTime fimDoDia = hoje.plusDays(1).atStartOfDay();

        Double total = pedidoRepository.calcularFaturamentoEntre(inicioDoDia, fimDoDia);

        return total == null ? 0.0 : total;
    }
    
}