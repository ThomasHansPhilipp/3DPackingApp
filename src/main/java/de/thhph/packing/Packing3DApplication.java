package de.thhph.packing;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 *
 */
@SuppressWarnings("serial")
@SpringBootApplication
@Theme(value = "packing3d-app")
public class Packing3DApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(Packing3DApplication.class, args);
	}

}
