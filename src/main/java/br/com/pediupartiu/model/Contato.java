package br.com.pediupartiu.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

//É nesta classe que os contatos dos clientes são representados, armazenando os telefones cadastrados no sistema.
@Entity
@Table(name = "Contato")
public class Contato {

    //Atributos do contato e seu relacionamento com o cliente.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Contato")
    private Integer idContato;

    @Column(name = "Telefone", length = 25)
    private String telefone;

    @ManyToOne
    @JoinColumn(name = "ID_Cliente")
    private Cliente cliente;

    //Construtores da classe.
    public Contato() {
    }

    public Contato(String telefone, Cliente cliente) {
        this.telefone = telefone;
        this.cliente = cliente;
    }

    //Gets e sets utilizados para acessar e alterar os dados do contato.
    public Integer getIdContato() {
        return idContato;
    }

    public void setIdContato(Integer idContato) {
        this.idContato = idContato;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}