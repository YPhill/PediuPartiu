
package br.com.pediupartiu.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AcessoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        var session = request.getSession(false);

        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        String perfil = (String) session.getAttribute("perfilUsuario");

        if (!"ADM".equals(perfil) && !"COZINHA".equals(perfil)) {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        return true;
    }
}
