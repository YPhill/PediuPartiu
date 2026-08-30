package br.com.pediupartiu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//Classe principal do sistema. É por meio dela que a aplicação Spring Boot é iniciada.
@SpringBootApplication
public class PediuPartiuApplication {

	//Método principal do projeto. Ao executar a aplicação, o Spring Boot é carregado e todos os recursos do sistema são inicializados.
	public static void main(String[] args) {
		SpringApplication.run(PediuPartiuApplication.class, args);
	}

}