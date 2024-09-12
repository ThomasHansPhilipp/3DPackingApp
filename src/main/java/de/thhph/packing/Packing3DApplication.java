package de.thhph.packing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;

/**
 * The entry point of the Spring Boot application.
 *
 */
@SuppressWarnings("serial")
@SpringBootApplication
@Theme(value = "packing3d-app")
@Push(PushMode.MANUAL)
public class Packing3DApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		SpringApplication.run(Packing3DApplication.class, args);
	}

}
