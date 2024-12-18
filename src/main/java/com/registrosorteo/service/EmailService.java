package com.registrosorteo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.util.List;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	// Método para enviar correo con archivos adjuntos, acepta una lista de rutas
	public void enviarCorreo(String destinatario, String asunto, String cuerpo, List<String> rutasArchivos) {
		try {
			MimeMessage mensaje = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mensaje, true); // true permite adjuntar archivos

			helper.setTo(destinatario);
			helper.setSubject(asunto);
			helper.setText(cuerpo);

			// Iterar sobre las rutas de archivos y adjuntar cada uno
			for (String rutaArchivo : rutasArchivos) {
				File archivo = new File(rutaArchivo);

				// Verifica si el archivo existe
				if (archivo.exists()) {
					FileSystemResource archivoAdjunto = new FileSystemResource(archivo);
					String nombreArchivo = archivo.getName(); // Puedes cambiar el nombre del archivo si lo deseas
					helper.addAttachment(nombreArchivo, archivoAdjunto);
				}
			}

			// Enviar el correo
			mailSender.send(mensaje);

		} catch (MessagingException e) {
			throw new RuntimeException("Error al enviar correo con archivo adjunto: " + e.getMessage(), e);
		}
	}
}
