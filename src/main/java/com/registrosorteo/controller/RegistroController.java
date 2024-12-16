package com.registrosorteo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.registrosorteo.dto.RegistroRequest;
import com.registrosorteo.service.GoogleSheetsService;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/api")
public class RegistroController {

	@Autowired
	private GoogleSheetsService googleSheetsService;

	@PostMapping("/registrar")
	public ResponseEntity<String> registrar(@RequestBody RegistroRequest request) {
		try {
			googleSheetsService.addRecordToSheet(request.getNombre(), request.getDni(), request.getCelularPrincipal(),
					request.getCelularOpcional(), request.getCorreoPrincipal(), request.getCorreoOpcional(),
					request.getEmpresa(), request.isAutorizacion(), request.isAfiliacion());
			return ResponseEntity.ok("Registro guardado exitosamente!");
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.ok("Error al guardar el registro: " + e.getMessage());
		}
	}
}
