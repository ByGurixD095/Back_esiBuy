package esi.grupo5.esiBuy;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/** Configura esiBuy para su despliegue en un contenedor de servlets externo. */
public class ServletInitializer extends SpringBootServletInitializer {

	/**
	 * Registra la clase principal de la aplicación en el constructor de Spring Boot.
	 *
	 * @param application constructor de la aplicación web
	 * @return constructor configurado con {@link EsiBuyApplication}
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(EsiBuyApplication.class);
	}

}
