package es.neifi.controlfitAPI.rest.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import es.neifi.controlfitAPI.rest.exceptions.ApiError;
import es.neifi.controlfitAPI.rest.exceptions.ClienteNotFoundException;
import es.neifi.controlfitAPI.rest.exceptions.FalloEnAltaClienteException;
import es.neifi.controlfitAPI.rest.model.cliente.Cliente;
import es.neifi.controlfitAPI.rest.model.cliente.ClienteRepository;
import es.neifi.controlfitAPI.rest.model.dto.CrearUsuarioDTO;
import es.neifi.controlfitAPI.rest.model.dto.InfoClienteDTO;
import es.neifi.controlfitAPI.rest.model.dto.converter.ClientDetailsDTOConverter;
import es.neifi.controlfitAPI.rest.model.dto.converter.CreateClienteDTOConverter;
import es.neifi.controlfitAPI.rest.model.dto.converter.EditarClienteDTOConverter;
import es.neifi.controlfitAPI.rest.model.gimnasio.GimnasioRepository;
import es.neifi.controlfitAPI.rest.model.registrohorario.RegistroHorarioRepository;
import es.neifi.controlfitAPI.rest.model.usuario.Usuario;
import es.neifi.controlfitAPI.rest.services.UsuarioService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping({ "/api" })
@RequiredArgsConstructor
public class ClienteController {

	private final ClienteRepository clienteRepository;
	private final ClientDetailsDTOConverter clienteDetailsDTOConverter;
	private final CreateClienteDTOConverter createClienteDTOConverter;
	private final EditarClienteDTOConverter editarClienteDTOConverter;
	private final UsuarioController usuarioController;
	private final GimnasioRepository gimnasioRepository;
	private final RegistroHorarioRepository registroHorarioRepository;
	private final UsuarioService usuarioService;

	/**
	 * Obtener todos los clientes
	 * 
	 * @return 404 si no hay ningun cliente 200 si ha encontrado algún cliente
	 */
	@ApiOperation(value = "Obtener lista completa de clientes", notes = "Obtener todos los clientes")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })

	@GetMapping("/cliente")
	public ResponseEntity<?> obtenerTodos() {

		List<Cliente> clientes = clienteRepository.findAllByOrderByIdAsc();
		if (clientes.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay clientes registrados");
		} else {
			List<InfoClienteDTO> dtoList = clientes.stream().map(clienteDetailsDTOConverter::convertToDTO)
					.collect(Collectors.toList());
			return ResponseEntity.ok(dtoList);
		}

	}

	@ApiOperation(value = "Obtener un cliente por la id", notes = "permite obtener datos de un solo cliente pasando la id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })
	@GetMapping("/cliente/{id}")
	public Cliente obtenerUno(
			@ApiParam(value = "ID del cliente", required = true, type = "Integer") @PathVariable int id) {
		try {

			return clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		} catch (ClienteNotFoundException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
		}
	}

	@ApiOperation(value = "Obtener un cliente por la id", notes = "permite obtener datos de un solo cliente pasando la id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })
	@GetMapping("/cliente/me")
	public Cliente obtenerMe(
			@ApiParam(value = "ID del cliente", required = true, type = "Integer") @AuthenticationPrincipal Usuario cliente) {
		try {

			return clienteRepository.findById(cliente.getId_usuario())
					.orElseThrow(() -> new ClienteNotFoundException(cliente.getId_usuario()));
		} catch (ClienteNotFoundException ex) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
		}
	}

	@ApiOperation(value = "Añadir cliente", notes = "Permite añadir un cliente, al ser añadido"
			+ " se crea automaticamente un usuario con el nombre del cliente como credencias, tanto de usuario como"
			+ " de contraseña, la id del gimnasio se obtiene a partir del usuario que le dio de alta que solo podrá "
			+ " ser un admin")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })
	@PostMapping("/cliente")
	public ResponseEntity<Cliente> crearCliente(
			@ApiParam(value = "Datos del cliente", required = true, type = "JSON") @RequestBody Cliente nuevo,
			@AuthenticationPrincipal Usuario admin) {
		
		//Busca id del gimnasio a partir del usuario que da el alta
		int id_gimnasio = clienteRepository.findIdGimnasioByIdUsuario(admin.getId_usuario());
		nuevo.setId_gimnasio(id_gimnasio);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		nuevo.setFecha_inscripcion(
				simpleDateFormat.format(new Date()));
		
//		Cliente saved = createClienteDTOConverter.convertToClient(nuevo);
		clienteRepository.save(nuevo);
		
		// Alta usuario automatica
		if (clienteRepository.findById(nuevo.getId()).isPresent()) {
			usuarioController.nuevoUsuario(CrearUsuarioDTO.builder().username(nuevo.getNombre()).password(nuevo.getNombre())
					.build());
			return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
		}else {
			throw new FalloEnAltaClienteException(new ClienteNotFoundException());
			
		}
		
	}
	
	
	@ApiOperation(value = "Edita un cliente", notes = "Permite editar un cliente por la autenticacion")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })
	@PutMapping("/cliente")
	public Cliente modificarCliente(
			@ApiParam(value = "Datos del cliente", required = true, type = "JSON") @RequestBody Cliente cliente,
			@AuthenticationPrincipal Usuario usuario) {

		cliente.setId(usuario.getId_usuario());
		System.out.println(cliente.getCalle());
		return clienteRepository.save(cliente);

	}

	@ApiOperation(value = "Edita un cliente", notes = "Permite editar un cliente por la id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })

	@PutMapping("/cliente/{id}")
	public Cliente modificarClienteporId(
			@ApiParam(value = "Datos del cliente", required = true, type = "JSON") @RequestBody Cliente cliente,
			@PathVariable int id) {

		cliente.setId(id);
		return clienteRepository.save(cliente);

	}

	@ApiOperation(value = "Borrar cliente", notes = "Permite borrar un cliente por la id")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = Cliente.class),
			@ApiResponse(code = 404, message = "Not Found", response = ApiError.class),
			@ApiResponse(code = 500, message = "Internal Server Error", response = ApiError.class) })
	@DeleteMapping("/cliente/{id}")
	public ResponseEntity<?> borrarProducto(
			@ApiParam(value = "ID del cliente", required = true, type = "Integer") @PathVariable int id) {
		Cliente borrar = clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException(id));
		registroHorarioRepository.deleteByIdUsuario(id);
		clienteRepository.delete(borrar);
		usuarioService.deleteById(id);
		return ResponseEntity.noContent().build();
	}



}
