package br.com.pediupartiu.model;

import java.util.List;
import jakarta.persistence.Transient;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

//É nesta classe que os clientes cadastrados no sistema são representados, juntamente com seus contatos.
@Entity
@Table(name = "Cliente")
public class Cliente {

    //Atributos do cliente e seus relacionamentos.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Cliente")
    private Integer idCliente;

    @Column(name = "Nome_Cliente", length = 100, nullable = false)
    private String nomeCliente;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contato> contatos;

    //Construtores da classe.
    public Cliente() {
    }

    public Cliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    //Gets e sets utilizados para acessar e alterar os dados do cliente.
    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public List<Contato> getContatos() {
        return contatos;
    }

    public void setContatos(List<Contato> contatos) {
        this.contatos = contatos;
    }
    
    //Atributo utilizado apenas durante a execução do sistema para indicar se o cliente possui pedidos vinculados.
    @Transient
    private boolean possuiPedidos;

    public boolean isPossuiPedidos() {
        return possuiPedidos;
    }

    public void setPossuiPedidos(boolean possuiPedidos) {
        this.possuiPedidos = possuiPedidos;
    }
    
}