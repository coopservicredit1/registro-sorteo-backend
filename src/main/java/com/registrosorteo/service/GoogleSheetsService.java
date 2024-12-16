package com.registrosorteo.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class GoogleSheetsService {

	@Value("${google.sheets.spreadsheetId}")
	private String spreadsheetId;

	@Value("${google.cre.json}")
	private String googleCredentialsJson;

	private Sheets sheetsService;

	// Método para autenticar y obtener servicio de Google Sheets
	public Sheets getSheetsService() throws GeneralSecurityException, IOException {
		String credentils = new String(Base64.getDecoder().decode(googleCredentialsJson));
		
		if (sheetsService == null) {
			GoogleCredential credential = GoogleCredential
					.fromStream(new ByteArrayInputStream(credentils.getBytes()))
					.createScoped(Arrays.asList("https://www.googleapis.com/auth/spreadsheets"));

			sheetsService = new Sheets.Builder(credential.getTransport(), credential.getJsonFactory(), credential)
					.setApplicationName("registro-sorteo").build();
		}
		return sheetsService;
	}

	// Método para agregar un registro en la hoja de Google Sheets
	public void addRecordToSheet(String nombre, String dni, String celularPrincipal, String celularOpcional,
			String correoPrincipal, String correoOpcional, String empresa, boolean autorizacion, boolean afiliacion)
			throws IOException, GeneralSecurityException {

		List<List<Object>> newRecord = Arrays.asList(
				Arrays.asList(nombre, dni, celularPrincipal, celularOpcional, correoPrincipal, correoOpcional, empresa,
						autorizacion ? "Sí" : "No", afiliacion ? "Sí" : "No", new java.util.Date().toString()));

		// Definir rango y valores
		ValueRange body = new ValueRange().setValues(newRecord);

		// Llamar al servicio de Google Sheets para agregar la fila
		getSheetsService().spreadsheets().values().append(spreadsheetId, "Registros!A1", body)
				.setValueInputOption("RAW").execute();
	}
}
