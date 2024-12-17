package com.registrosorteo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.registrosorteo.dto.RegistroRequest;
import com.registrosorteo.Constantes;
import com.registrosorteo.dto.ApiResponse;
import com.registrosorteo.service.GoogleSheetsService;

@RestController
@RequestMapping("/api")
public class RegistroController {

	@Autowired
	private GoogleSheetsService googleSheetsService;

	@PostMapping("/registrar")
	public ResponseEntity<ApiResponse<String>> registrar(@RequestBody RegistroRequest request) {
		try {
			// Llama al servicio para agregar el registro
			ApiResponse<String> response = googleSheetsService.addRecordToSheet(request.getNombre(), request.getDni(),
					request.getCelularPrincipal(), request.getCelularOpcional(), request.getCorreoPrincipal(),
					request.getCorreoOpcional(), request.getEmpresa(), request.isAutorizacion(),
					request.isAfiliacion());

			// Devuelve la respuesta generada por el servicio
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			e.printStackTrace();

			// Devuelve un error con código 0
			ApiResponse<String> errorResponse = new ApiResponse<>(Constantes.RESPONSE_CODE_ERROR,
					"Error al guardar el registro: " + e.getMessage());
			return ResponseEntity.status(500).body(errorResponse);
		}
	}
}
