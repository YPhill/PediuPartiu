
package br.com.pediupartiu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AcessoInterceptor acessoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(acessoInterceptor)
                .excludePathPatterns(
                        "/",
                        "/login",
                        "/sair",
                        "/error",
                        "/api/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/favicon.ico"
                );
    }
}
