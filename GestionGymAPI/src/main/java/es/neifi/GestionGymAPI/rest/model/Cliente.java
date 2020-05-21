package es.neifi.GestionGymAPI.rest.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity

@Component
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private int id_cliente;
	private String dni;
	private String nombre;
	private String apellidos;
	private String fecha_nacimiento;
	private String fecha_inscripcion;
	private String email;
	private String calle;
	private String codigo_postal;
	private String ciudad;
	private String provincia;

	
//	@ManyToOne(targetEntity = Gimnasio.class)
//	@JoinColumn(name="id_gimnasio")
//	private Gimnasio id_gimnasio;//TODo Trigger
	
	@JsonIgnore
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="id_cliente", referencedColumnName = "id_usuario")
	private Usuario usuario;
	
	
	
	

	
}
