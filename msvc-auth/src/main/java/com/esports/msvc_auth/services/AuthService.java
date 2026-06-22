package com.esports.msvc_auth.services;

import com.esports.msvc_auth.clients.UserClient;
import com.esports.msvc_auth.models.AuthUser;
import com.esports.msvc_auth.models.Rol;
import com.esports.msvc_auth.models.dtos.AuthAccount;
import com.esports.msvc_auth.models.dtos.AuthResponseDTO;
import com.esports.msvc_auth.models.dtos.LoginRequestDTO;
import com.esports.msvc_auth.models.dtos.RegisterRequestDTO;
import com.esports.msvc_auth.repositories.AuthUserRepository;
import com.esports.msvc_auth.repositories.RolRepository;
import com.esports.msvc_auth.security.JwtService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Logica de autenticacion y gestion de cuentas de acceso.
 *
 * Una cuenta tiene uno o mas roles (Set<Rol>, relacion muchos-a-muchos).
 *
 *  - register(): crea la CuentaAcceso con sus roles, le pide a msvc-user
 *    (via Feign) que cree el perfil del usuario y devuelve un JWT.
 *  - login(): valida email + password contra BCrypt y devuelve un JWT.
 *  - CRUD de cuentas (listar, buscar, actualizar password/roles/estado, desactivar).
 *
 * BCrypt es obligatorio. El email es unico.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthUserRepository authUserRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserClient userClient;

    public AuthService(AuthUserRepository authUserRepository, RolRepository rolRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, UserClient userClient) {
        this.authUserRepository = authUserRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userClient = userClient;
    }



    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }


        // 2) Si el cliente no indico roles, se asigna JUGADOR por defecto.
        Set<String> nombresRoles = (request.getRoles() == null || request.getRoles().isEmpty())
                ? Set.of("ROLE_JUGADOR")
                : request.getRoles();

        // 3) Buscar cada rol en la BD. Si piden un rol que no existe, se rechaza (400).
        Set<Rol> roles = nombresRoles.stream()
                .map(this::normalizarRol)
                .map(nombre -> this.rolRepository.findByNombre(nombre).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no existe: " + nombre)))
                .collect(Collectors.toCollection(HashSet::new));

        AuthUser cuenta = new AuthUser();
        cuenta.setEmail(request.getEmail());
        cuenta.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        cuenta.setRoles(roles);
        cuenta.setEstado("ACTIVO");
        AuthUser guardada = authUserRepository.save(cuenta);
        log.info("Cuenta de acceso creada: id={}, email={}, roles={}",
                guardada.getCuentaAccesoId(), guardada.getEmail(),
                guardada.getRoles().stream().map(Rol::getNombre).toList());

        // Cumplir "auth consulta user-service": crear el perfil en msvc-user.
        // Es best-effort: si el perfil ya existe (409) o user-service esta caido,
        // la cuenta de acceso igual queda creada.
        // Se envia el rol principal (el primero, sin prefijo ROLE_) porque msvc-user
        // guarda un solo "rol" segun el caso semestral.
        String rolPrincipal = roles.stream().map(Rol::getNombre).findFirst().orElse("ROLE_JUGADOR");
        String rolParaPerfil = rolPrincipal.replace("ROLE_", "");
        Map<String, Object> perfil = new HashMap<>();
        perfil.put("nombre", request.getNombre());
        perfil.put("nickname", request.getNickname());
        perfil.put("email", request.getEmail());
        perfil.put("rol", rolParaPerfil);
        try {
            userClient.crearPerfil(perfil);
            log.info("Perfil de usuario creado en msvc-user para email={}", request.getEmail());
        } catch (FeignException.Conflict e) {
            log.warn("Perfil ya existia en msvc-user para email={}: {}", request.getEmail(), e.getMessage());
        } catch (FeignException e) {
            log.warn("No se pudo crear el perfil en msvc-user para email={}: {}",
                    request.getEmail(), e.getMessage());
        }

        return construirRespuestaConToken(guardada);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        AuthUser cuenta = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        if (!"ACTIVO".equalsIgnoreCase(cuenta.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta inactiva");
        }

        if (!passwordEncoder.matches(request.getPassword(), cuenta.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        return construirRespuestaConToken(cuenta);
    }

    /** Respuesta con token JWT (para login y register). */
    private AuthResponseDTO construirRespuestaConToken(AuthUser cuenta) {

        String token = this.jwtService.generarToken(cuenta);
        Set<String> roles = cuenta.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet());
        return new AuthResponseDTO(token, "Bearer", cuenta.getEmail(), roles);
    }

    // ===================== CRUD de cuentas (ADMIN) =====================

    @Transactional(readOnly = true)
    public List<AuthAccount> findAll(String rol, String estado) {
        String rolNormalizado = (rol != null) ? normalizarRol(rol) : null;

        if (rolNormalizado != null && estado != null) {
            return authUserRepository.findByRolNombreAndEstado(rolNormalizado, estado)
                    .stream().map(this::toResponse).toList();
        } else if (rolNormalizado != null) {
            return authUserRepository.findByRolNombre(rolNormalizado)
                    .stream().map(this::toResponse).toList();
        } else if (estado != null) {
            return authUserRepository.findByEstado(estado).stream().map(this::toResponse).toList();
        }
        return authUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AuthAccount findById(Long id) {
        return authUserRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public AuthAccount findByEmail(String email) {
        return authUserRepository.findByEmail(email).map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + email));
    }

    @Transactional
    public AuthAccount updatePassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "newPassword es obligatorio");
        }

        AuthUser cuenta = authUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + id));
        cuenta.setPasswordHash(passwordEncoder.encode(newPassword));
        log.info("Password actualizada para cuenta id={}", id);
        return toResponse(authUserRepository.save(cuenta));
    }

    /** Reemplaza todos los roles de una cuenta por el set enviado. */
    @Transactional
    public AuthAccount updateRol(Long id, Set<String> nuevosRoles) {
        if (nuevosRoles == null || nuevosRoles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nuevosRoles no puede estar vacio");
        }
        AuthUser cuenta = authUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + id));

        Set<Rol> roles = nuevosRoles.stream()
                .map(this::normalizarRol)
                .map(nombre -> this.rolRepository.findByNombre(nombre).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no existe: " + nombre)))
                .collect(Collectors.toCollection(HashSet::new));

        cuenta.setRoles(roles);
        log.info("Roles actualizados para cuenta id={} -> {}",
                id, roles.stream().map(Rol::getNombre).toList());
        return toResponse(authUserRepository.save(cuenta));
    }

    @Transactional
    public AuthAccount updateEstado(Long id, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nuevoEstado es obligatorio");
        }
        AuthUser cuenta = authUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + id));
        cuenta.setEstado(nuevoEstado.trim().toUpperCase(Locale.ROOT));
        log.info("Estado actualizado para cuenta id={} -> {}", id, cuenta.getEstado());
        return toResponse(authUserRepository.save(cuenta));
    }

    @Transactional
    public AuthAccount desactivar(Long id) {
        AuthUser cuenta = authUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada: " + id));
        cuenta.setEstado("INACTIVO");
        log.info("Cuenta desactivada: id={}", id);
        return toResponse(authUserRepository.save(cuenta));
    }



    /** Respuesta SIN token (para endpoints de gestion de cuentas). */
    private AuthAccount toResponse(AuthUser c) {
        AuthAccount r = new AuthAccount();
        r.setCuentaAccesoId(c.getCuentaAccesoId());
        r.setEmail(c.getEmail());
        r.setRoles(c.getRoles().stream().map(Rol::getNombre).collect(Collectors.toSet()));
        r.setEstado(c.getEstado());
        return r;
    }




    /** Normaliza un rol a ROLE_X (ej: "administrador" -> "ROLE_ADMINISTRADOR"). */
    private String normalizarRol(String rol) {
        String limpio = rol.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
        return limpio.startsWith("ROLE_") ? limpio : "ROLE_" + limpio;
    }
}
