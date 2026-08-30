package br.com.pediupartiu.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

//É nesta classe que os pedidos realizados no sistema são representados, armazenando informações do cliente, pagamento, status e itens do pedido.
@Entity
@Table(name = "Pedido")
public class Pedido {

    //Atributos do pedido e seus relacionamentos com as demais entidades.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Pedido")
    private Integer idPedido;

    @ManyToOne
    @JoinColumn(name = "ID_Pagamento")
    private FormaPagamento formaPagamento;

    @ManyToOne
    @JoinColumn(name = "ID_Cliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "ID_Status", nullable = false)
    private StatusPedido statusPedido;

    @Column(name = "DataHora_Pedido", insertable = false)
    private LocalDateTime dataHoraPedido;

    @Column(name = "Observacao", columnDefinition = "TEXT")
    private String observacao;

    @OneToMany(mappedBy = "pedido", fetch = FetchType.EAGER)
    private List<ItemPedido> itens;

    //Construtor padrão da classe.
    public Pedido() {
    }

    //Gets e sets utilizados para acessar e alterar os dados do pedido.
    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(StatusPedido statusPedido) {
        this.statusPedido = statusPedido;
    }

    public LocalDateTime getDataHoraPedido() {
        return dataHoraPedido;
    }

    public void setDataHoraPedido(LocalDateTime dataHoraPedido) {
        this.dataHoraPedido = dataHoraPedido;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }
}