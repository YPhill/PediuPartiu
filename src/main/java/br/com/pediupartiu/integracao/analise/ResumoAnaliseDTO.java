package br.com.pediupartiu.integracao.analise;

import java.util.List;
import java.util.Map;

public class ResumoAnaliseDTO {

    private long pedidosRegistrados;
    private long pedidosEntregues;
    private long pedidosCancelados;
    

    private double faturamento;
    private double ticketMedio;

    private List<Map<String, Object>> produtosMaisVendidos;
    private List<Map<String, Object>> faturamentoPorProduto;
    private List<Map<String, Object>> vendasPorDia;
    private List<Map<String, Object>> formasPagamento;
    
    public long getPedidosRegistrados() {
        return pedidosRegistrados;
    }

    public void setPedidosRegistrados(long pedidosRegistrados) {
        this.pedidosRegistrados = pedidosRegistrados;
    }

    public long getPedidosEntregues() {
        return pedidosEntregues;
    }

    public void setPedidosEntregues(long pedidosEntregues) {
        this.pedidosEntregues = pedidosEntregues;
    }

    public long getPedidosCancelados() {
        return pedidosCancelados;
    }

    public void setPedidosCancelados(long pedidosCancelados) {
        this.pedidosCancelados = pedidosCancelados;
    }

    public double getFaturamento() {
        return faturamento;
    }

    public void setFaturamento(double faturamento) {
        this.faturamento = faturamento;
    }

    public double getTicketMedio() {
        return ticketMedio;
    }

    public void setTicketMedio(double ticketMedio) {
        this.ticketMedio = ticketMedio;
    }

    public List<Map<String, Object>> getProdutosMaisVendidos() {
        return produtosMaisVendidos;
    }

    public void setProdutosMaisVendidos(
            List<Map<String, Object>> produtosMaisVendidos) {
        this.produtosMaisVendidos = produtosMaisVendidos;
    }

    public List<Map<String, Object>> getFaturamentoPorProduto() {
        return faturamentoPorProduto;
    }

    public void setFaturamentoPorProduto(
            List<Map<String, Object>> faturamentoPorProduto) {
        this.faturamentoPorProduto = faturamentoPorProduto;
    }
 // retorna as vendas agrupadas por dia
    public List<Map<String, Object>> getVendasPorDia() {
        return vendasPorDia;
    }

    // recebe as vendas agrupadas por dia
    public void setVendasPorDia(List<Map<String, Object>> vendasPorDia) {
        this.vendasPorDia = vendasPorDia;
    }
 // retorna as formas de pagamento
    public List<Map<String, Object>> getFormasPagamento() {
        return formasPagamento;
    }

    // recebe as formas de pagamento
    public void setFormasPagamento(
            List<Map<String, Object>> formasPagamento) {
        this.formasPagamento = formasPagamento;
    }
}