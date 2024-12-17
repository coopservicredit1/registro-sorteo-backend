package com.registrosorteo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class RegistroSorteoBackendApplication {

	public static void main(String[] args) {
		// Establece la zona horaria predeterminada
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
		SpringApplication.run(RegistroSorteoBackendApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Reafirma la zona horaria en el ciclo de vida del bean
		TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
	}
}
