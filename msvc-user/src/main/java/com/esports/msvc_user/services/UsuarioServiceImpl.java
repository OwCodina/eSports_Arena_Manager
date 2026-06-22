package com.esports.msvc_user.services;

import com.esports.msvc_user.exceptions.UserException;
import com.esports.msvc_user.models.Usuario;
import com.esports.msvc_user.models.dtos.UsuarioRequestDTO;
import com.esports.msvc_user.models.dtos.UsuarioResponseDTO;
import com.esports.msvc_user.repositories.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UsuarioResponseDTO toResponse(Usuario u) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setUsuarioId(u.getUsuarioId());
        dto.setNombre(u.getNombre());
        dto.setNickname(u.getNickname());
        dto.setEmail(u.getEmail());
        dto.setRol(u.getRol());
        dto.setEstado(u.getEstado());
        dto.setFechaRegistro(u.getFechaRegistro());
        if (u.getAudit() != null) {
            dto.setCreatedAt(u.getAudit().getCreatedAt());
            dto.setUpdatedAt(u.getAudit().getUpdatedAt());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponseDTO> findAll() {
        log.info("Listando todos los usuarios");
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponseDTO> findByRol(String rol) {
        return usuarioRepository.findByRol(rol).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponseDTO> findByEstado(String estado) {
        return usuarioRepository.findByEstado(estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<UsuarioResponseDTO> findByRolAndEstado(String rol, String estado) {
        return usuarioRepository.findByRolAndEstado(rol, estado).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioResponseDTO findById(Long id) {
        return usuarioRepository.findById(id).map(this::toResponse).orElseThrow(() -> {
            log.error("Usuario con id {} no encontrado", id);
            return new UserException("Usuario con id: " + id + " no encontrado");
        });
    }

    @Transactional(readOnly = true)
    @Override
    public UsuarioResponseDTO findByNickname(String nickname) {
        return usuarioRepository.findByNickname(nickname).map(this::toResponse).orElseThrow(() ->
                new UserException("Usuario con nickname: " + nickname + " no encontrado"));
    }

    @Transactional
    @Override
    public UsuarioResponseDTO save(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByNickname(dto.getNickname()).isPresent())
            throw new UserException("Ya existe un usuario con el nickname: " + dto.getNickname());
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent())
            throw new UserException("Ya existe un usuario con el email: " + dto.getEmail());

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setNickname(dto.getNickname());
        u.setEmail(dto.getEmail());
        u.setRol(dto.getRol());
        u.setEstado("ACTIVO");
        u.setFechaRegistro(LocalDate.now());
        Usuario guardado = usuarioRepository.save(u);
        log.info("Usuario creado: id={}, nickname={}", guardado.getUsuarioId(), guardado.getNickname());
        return toResponse(guardado);
    }

    @Transactional
    @Override
    public UsuarioResponseDTO updateById(Long id, UsuarioRequestDTO dto) {
        return usuarioRepository.findById(id).map(existing -> {
            if (dto.getNickname() != null && !dto.getNickname().equals(existing.getNickname())) {
                if (usuarioRepository.findByNickname(dto.getNickname()).isPresent())
                    throw new UserException("Ya existe un usuario con el nickname: " + dto.getNickname());
                existing.setNickname(dto.getNickname());
            }
            if (dto.getEmail() != null && !dto.getEmail().equals(existing.getEmail())) {
                if (usuarioRepository.findByEmail(dto.getEmail()).isPresent())
                    throw new UserException("Ya existe un usuario con el email: " + dto.getEmail());
                existing.setEmail(dto.getEmail());
            }
            if (dto.getNombre() != null) existing.setNombre(dto.getNombre());
            if (dto.getRol() != null)    existing.setRol(dto.getRol());
            log.info("Usuario actualizado: id={}", existing.getUsuarioId());
            return toResponse(usuarioRepository.save(existing));
        }).orElseThrow(() -> new UserException("Usuario con id: " + id + " no encontrado"));
    }

    @Transactional
    @Override
    public UsuarioResponseDTO desactivar(Long id) {
        return usuarioRepository.findById(id).map(existing -> {
            existing.setEstado("INACTIVO");
            log.info("Usuario desactivado: id={}", id);
            return toResponse(usuarioRepository.save(existing));
        }).orElseThrow(() -> new UserException("Usuario con id: " + id + " no encontrado"));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean puedeCompetir(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new UserException("Usuario con id: " + id + " no encontrado"));
        return "ACTIVO".equals(u.getEstado());
    }
}
