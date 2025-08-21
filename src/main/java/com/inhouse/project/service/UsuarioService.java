package com.inhouse.project.service;

import com.inhouse.project.domain.Proprietario;
import com.inhouse.project.domain.Usuarios;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.repository.ProprietarioRepository;
import com.inhouse.project.repository.UsuarioRepository;
import com.inhouse.project.service.dto.UsuarioRegisterDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuariosRepository;
    private final ProprietarioRepository proprietariosRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuarios register(UsuarioRegisterDTO dto) {
        // Mapeia o DTO para a entidade Usuarios
        Usuarios usuario = new Usuarios();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuario.setRole(dto.getRole());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Salva o usuário
        Usuarios usuarioSalvo = usuariosRepository.save(usuario);

        // Se for proprietário, cria também o registro em proprietarios
        if (dto.getRole() == Usuarios.Role.PROPRIETARIO) {
            Proprietario proprietario = new Proprietario();
            proprietario.setUsuario(usuarioSalvo);
            proprietario.setCpf(dto.getCpf());
            proprietario.setEndereco(dto.getEndereco());
            proprietario.setCidade(dto.getCidade());
            proprietario.setEstado(dto.getEstado());
            proprietario.setCep(dto.getCep());
            proprietariosRepository.save(proprietario);
        }

        return usuarioSalvo;
    }

    public List<UsuarioRegisterDTO> listarTodosUsuarios() {
        return usuariosRepository.findAll().stream().map(usuario -> {
            UsuarioRegisterDTO dto = new UsuarioRegisterDTO();
            dto.setNome(usuario.getNome());
            dto.setEmail(usuario.getEmail());
            dto.setTelefone(usuario.getTelefone());
            dto.setRole(usuario.getRole());

            // Se for proprietário, preencher os dados adicionais
            if (usuario.getRole() == Usuarios.Role.PROPRIETARIO) {
                proprietariosRepository.findByUsuarioId(usuario.getId()).ifPresent(proprietario -> {
                    dto.setCpf(proprietario.getCpf());
                    dto.setEndereco(proprietario.getEndereco());
                    dto.setCidade(proprietario.getCidade());
                    dto.setEstado(proprietario.getEstado());
                    dto.setCep(proprietario.getCep());
                });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public Usuarios getUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return usuariosRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
    }

    public Proprietario getProprietarioDoUsuario(Long usuarioId) {
        return proprietariosRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BusinessException("Proprietário não encontrado"));
    }
}
