package br.com.pediupartiu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

//É nesta classe que o acesso ao sistema é controlado, definindo os perfis de usuário e gerenciando o login.
@Controller
public class AcessoController {

    //Senha utilizada para acesso administrativo.
    private static final String SENHA_ADM = "dgrill@10";

    //Apresenta a tela inicial de acesso.
    @GetMapping("/")
    public String telaAcesso() {
        return "acesso/acesso";
    }

    //Recebe os dados informados pelo usuário, valida o perfil escolhido e inicia a sessão correspondente.
    @PostMapping("/login")
    public String login(
            @RequestParam String tipoAcesso,
            @RequestParam(required = false) String senha,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if ("ADM".equals(tipoAcesso)) {

            if (senha == null || !SENHA_ADM.equals(senha)) {
                redirectAttributes.addFlashAttribute("erro", "Senha inválida para acesso ADM.");
                return "redirect:/";
            }

            session.setAttribute("perfilUsuario", "ADM");

            return "redirect:/inicio";
        }

        if ("COZINHA".equals(tipoAcesso)) {
            session.setAttribute("perfilUsuario", "COZINHA");

            return "redirect:/inicio";
        }

        redirectAttributes.addFlashAttribute("erro", "Tipo de acesso inválido.");
        return "redirect:/";
    }

    //Realiza o encerramento da sessão e retorna para a tela de acesso.
    @GetMapping("/sair")
    public String sair(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}