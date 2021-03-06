package es.neifi.controlfitAPI.rest.model.dto;

import java.util.List;

import es.neifi.controlfitAPI.rest.model.registrohorario.RegistroHorario;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CrearClienteDTO {
	private int id_gimnasio;
	private String dni;
	private String password;
	private String nombre;
	private String apellidos;
	private String email;
	private String calle;
	private String ciudad;
	private String provincia;
	private String codigo_postal;
	private String fecha_nacimiento;
	private String fecha_inscripcion;
}
