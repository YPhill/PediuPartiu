package br.com.pediupartiu.excecao;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.transaction.CannotCreateTransactionException;

//É nesta classe que os principais erros relacionados ao banco de dados são tratados, exibindo mensagens amigáveis para o usuário.
@ControllerAdvice
public class ConexaoBanco {

    //Trata situações em que o sistema não consegue se conectar ao banco de dados.
    @ExceptionHandler({DataAccessResourceFailureException.class, CannotCreateTransactionException.class})
    public String tratarBancoForaDoAr(Exception exception, Model model) {
        model.addAttribute("titulo", "Banco fora do ar");
        model.addAttribute("mensagem", "Não foi possível conectar ao banco de dados. Verifique se o MySQL está aberto e tente novamente.");
        return "erro/sistema";
    }

    //Trata operações que não podem ser realizadas devido às regras definidas no banco de dados.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String tratarRegraDoBanco(DataIntegrityViolationException exception, Model model) {
        model.addAttribute("titulo", "Ação não permitida");
        model.addAttribute("mensagem", "Ação não permitida pelas regras do banco de dados.");
        return "erro/sistema";
    }
}