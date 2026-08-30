package br.com.pediupartiu.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//É nesta classe que os possíveis status dos pedidos são representados dentro do sistema.
@Entity
@Table(name = "Status_Pedido")
public class StatusPedido {

    //Atributos do status do pedido.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Status")
    private Integer idStatus;

    @Column(name = "Nome_Status", length = 30, nullable = false)
    private String nomeStatus;

    //Construtores da classe.
    public StatusPedido() {
    }

    public StatusPedido(String nomeStatus) {
        this.nomeStatus = nomeStatus;
    }

    //Gets e sets utilizados para acessar e alterar os dados do status.
    public Integer getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Integer idStatus) {
        this.idStatus = idStatus;
    }

    public String getNomeStatus() {
        return nomeStatus;
    }

    public void setNomeStatus(String nomeStatus) {
        this.nomeStatus = nomeStatus;
    }
}