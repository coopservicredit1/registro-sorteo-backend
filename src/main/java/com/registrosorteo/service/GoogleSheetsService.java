package com.registrosorteo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.registrosorteo.Constantes;
import com.registrosorteo.dto.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class GoogleSheetsService {

	@Value("${google.sheets.spreadsheetId}")
	private String spreadsheetId;

	@Value("${google.cre.json}")
	private String googleCredentialsJson;

	private Sheets sheetsService;

	@Autowired
	private EmailService emailService;

	// Crear el objeto Logger
	private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsService.class);

	// Método para autenticar y obtener servicio de Google Sheets
	@SuppressWarnings("deprecation")
	public Sheets getSheetsService() throws GeneralSecurityException, IOException {
		if (sheetsService == null) {
			GoogleCredential credential = GoogleCredential
					.fromStream(new ByteArrayInputStream(googleCredentialsJson.getBytes()))
					.createScoped(Arrays.asList("https://www.googleapis.com/auth/spreadsheets"));

			sheetsService = new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
					.setApplicationName("registro-sorteo").build();
		}
		return sheetsService;
	}

	public ApiResponse<String> addRecordToSheet(String nombre, String dni, String celularPrincipal,
			String celularOpcional, String correoPrincipal, String correoOpcional, String empresa, boolean autorizacion,
			boolean afiliacion) {
		try {
			// Obtener la hora actual en horario peruano
			String horaActual = getHora();
			LocalDateTime fechaHoraActual = LocalDateTime.parse(horaActual,
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			// Obtener los sorteos válidos
			String sorteoValido = obtenerSorteoValido(fechaHoraActual);
			logger.info("Sorteo Valido: {}", sorteoValido);

			if (sorteoValido == null) {
				return new ApiResponse<>(Constantes.RESPONSE_CODE_ERROR,
						"No hay sorteos válidos para la fecha y hora actual.");
			}

			// Validar si el DNI ya está registrado en el sorteo válido
			if (dniRegistradoEnSorteo(sorteoValido, dni)) {
				return new ApiResponse<>(Constantes.RESPONSE_CODE_ERROR,
						"El DNI ya está registrado en el sorteo actual.");
			}

			// Crear una lista mutable para los nuevos registros
			List<List<Object>> newRecord = new ArrayList<>();

			// Agregar el registro principal
			newRecord.add(Arrays.asList(nombre, dni, celularPrincipal, celularOpcional, correoPrincipal, correoOpcional,
					empresa, autorizacion ? "Sí" : "No", afiliacion ? "Sí" : "No", horaActual, sorteoValido));

			// Agregar registros adicionales si el usuario está afiliado
			if (afiliacion) {
				for (int i = 0; i < 2; i++) { // Añadir 2 entradas adicionales
					newRecord.add(Arrays.asList(nombre, dni, celularPrincipal, celularOpcional, correoPrincipal,
							correoOpcional, empresa, autorizacion ? "Sí" : "No", afiliacion ? "Sí" : "No", horaActual,
							sorteoValido));
				}
			}

			logger.info("Registros a insertar: {}", newRecord);

			// Definir rango y valores
			ValueRange body = new ValueRange().setValues(newRecord);

			// Llamar al servicio de Google Sheets para agregar las filas
			getSheetsService().spreadsheets().values().append(spreadsheetId, "Registros!A1", body)
					.setValueInputOption("RAW").execute();

			try {
				// Lista de rutas de los archivos a adjuntar
				List<String> rutasArchivos = new ArrayList<>();
				rutasArchivos.add("src/main/resources/pdf/Servicredit-politicas-de-privacidad-uso-de-datos.pdf");
				if (afiliacion) {
					rutasArchivos.add("src/main/resources/pdf/solicitud-afiliacion-socios.pdf");
				}

				// Enviar correo al cliente
				String asunto = "Confirmación de Registro a Servicredit";
				String cuerpo = String.format(
						"Hola %s,\n\nGracias por registrarte en el sorteo.\n\nTus datos:\n- DNI: %s\n- Celular: %s\n- Correo: %s\n\n¡Buena suerte!",
						nombre, dni, celularPrincipal, correoPrincipal);
				emailService.enviarCorreo(correoPrincipal, asunto, cuerpo, rutasArchivos);

			} catch (Exception e) {
				logger.error(
						"Hubo un problema al enviar el correo para el sorteo: {} - para el cliente con DNI: {} - ErrorMessage: {}",
						sorteoValido, dni, e.getMessage());
			}

			return new ApiResponse<>(Constantes.RESPONSE_CODE_OK,
					afiliacion ? "Registro agregado exitosamente. ¡Tus opciones se han triplicado!"
							: "Registro agregado exitosamente.",
					sorteoValido);
		} catch (Exception e) {
			logger.error("Error al registrar los datos: {}", e.getMessage(), e);
			return new ApiResponse<>(Constantes.RESPONSE_CODE_ERROR,
					"Ocurrió un error al agregar el registro: " + e.getMessage());
		}
	}

	/**
	 * Verifica si el DNI ya está registrado en un sorteo específico.
	 */
	private boolean dniRegistradoEnSorteo(String sorteoValido, String dni) {
		try {
			// Leer todos los registros de la hoja
			// Ajusta el rango si es necesario
			ValueRange response = getSheetsService().spreadsheets().values().get(spreadsheetId, "Registros!A1:Z")
					.execute();

			List<List<Object>> values = response.getValues();

			if (values == null || values.isEmpty()) {
				return false; // No hay registros
			}

			// Iterar sobre los registros para buscar coincidencias
			for (List<Object> row : values) {
				if (row.size() >= 11) { // Asegurarse de que la fila tenga suficientes columnas
					String dniExistente = row.get(1).toString(); // Suponiendo que el DNI está en la columna 2
					String sorteoExistente = row.get(10).toString(); // Suponiendo que el sorteo está en la columna 11

					if (dni.equals(dniExistente) && sorteoValido.equals(sorteoExistente)) {
						return true; // DNI ya registrado en el sorteo actual
					}
				}
			}

			return false;
		} catch (Exception e) {
			logger.error("Error al verificar el DNI en el sorteo: {}", e.getMessage(), e);
			return false;
		}
	}

	// Método para obtener el sorteo válido
	private String obtenerSorteoValido(LocalDateTime fechaHoraActual) throws IOException, GeneralSecurityException {
		// Leer la hoja "Horarios"
		// Asegúrate de que los datos comiencen desde A2
		ValueRange response = getSheetsService().spreadsheets().values().get(spreadsheetId, "Horarios!A2:E").execute();

		List<List<Object>> sorteos = response.getValues();

		if (sorteos == null || sorteos.isEmpty()) {
			logger.warn("No se encontraron sorteos en la hoja Horarios.");
			return null;
		}

		// Iterar sobre la lista de sorteos
		for (List<Object> sorteo : sorteos) {
			String nombreSorteo = sorteo.get(0).toString(); // Columna "Sorteo"
			LocalDateTime inicio = parsearFechaHora(sorteo.get(1).toString(), sorteo.get(2).toString()); // Inicio
			LocalDateTime fin = parsearFechaHora(sorteo.get(3).toString(), sorteo.get(4).toString()); // Fin

			// Verificar si la fecha y hora actual están dentro del rango
			if (!fechaHoraActual.isBefore(inicio) && !fechaHoraActual.isAfter(fin)) {
				return nombreSorteo;
			}
		}

		return null; // No se encontró un sorteo válido
	}

	// Método para parsear la fecha y hora de inicio/fin del sorteo
	private LocalDateTime parsearFechaHora(String fecha, String hora) {
		String fechaHora = fecha + " " + hora;
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		return LocalDateTime.parse(fechaHora, formatter);
	}

	// Método para obtener la hora actual en formato peruano
	public static String getHora() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("America/Lima"));
		return sdf.format(new Date());
	}
}
