package br.com.pediupartiu.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

//É nesta classe que as formas de pagamento disponíveis no sistema são representadas.
@Entity
@Table(name = "Forma_Pagamento")
public class FormaPagamento {

    //Atributos da forma de pagamento.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_Pagamento")
    private Integer idPagamento;

    @Column(name = "Nome_Forma_Pagamento", length = 50, nullable = false)
    private String nomeFormaPagamento;

    //Construtores da classe.
    public FormaPagamento() {
    }

    public FormaPagamento(String nomeFormaPagamento) {
        this.nomeFormaPagamento = nomeFormaPagamento;
    }

    //Gets e sets utilizados para acessar e alterar os dados da forma de pagamento.
    public Integer getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Integer idPagamento) {
        this.idPagamento = idPagamento;
    }

    public String getNomeFormaPagamento() {
        return nomeFormaPagamento;
    }

    public void setNomeFormaPagamento(String nomeFormaPagamento) {
        this.nomeFormaPagamento = nomeFormaPagamento;
    }
}