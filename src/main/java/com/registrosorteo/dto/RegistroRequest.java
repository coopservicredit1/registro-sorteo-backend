package com.registrosorteo.dto;

public class RegistroRequest {
	private String nombre;
	private String dni;
	private String celularPrincipal;
	private String celularOpcional;
	private String correoPrincipal;
	private String correoOpcional;
	private String empresa;
	private boolean autorizacion;
	private boolean afiliacion;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDni() {
		return dni;
	}

	public void setDni(String dni) {
		this.dni = dni;
	}

	public String getCelularPrincipal() {
		return celularPrincipal;
	}

	public void setCelularPrincipal(String celularPrincipal) {
		this.celularPrincipal = celularPrincipal;
	}

	public String getCelularOpcional() {
		return celularOpcional;
	}

	public void setCelularOpcional(String celularOpcional) {
		this.celularOpcional = celularOpcional;
	}

	public String getCorreoPrincipal() {
		return correoPrincipal;
	}

	public void setCorreoPrincipal(String correoPrincipal) {
		this.correoPrincipal = correoPrincipal;
	}

	public String getCorreoOpcional() {
		return correoOpcional;
	}

	public void setCorreoOpcional(String correoOpcional) {
		this.correoOpcional = correoOpcional;
	}

	public String getEmpresa() {
		return empresa;
	}

	public void setEmpresa(String empresa) {
		this.empresa = empresa;
	}

	public boolean isAutorizacion() {
		return autorizacion;
	}

	public void setAutorizacion(boolean autorizacion) {
		this.autorizacion = autorizacion;
	}

	public boolean isAfiliacion() {
		return afiliacion;
	}

	public void setAfiliacion(boolean afiliacion) {
		this.afiliacion = afiliacion;
	}

}
