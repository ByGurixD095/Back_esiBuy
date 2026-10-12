package esi.grupo5.esiBuy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Punto de entrada de la aplicación Spring Boot de esiBuy. */
@SpringBootApplication
@EnableMongoAuditing
public class EsiBuyApplication {

	/**
	 * Inicia la aplicación y el contexto de Spring.
	 *
	 * @param args argumentos recibidos al arrancar el proceso
	 */
	public static void main(String[] args) {
		SpringApplication.run(EsiBuyApplication.class, args);
	}

}
