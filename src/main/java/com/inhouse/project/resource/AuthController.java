package com.inhouse.project.resource;

import com.inhouse.project.domain.Usuarios;
import com.inhouse.project.repository.UsuarioRepository;
import com.inhouse.project.security.JwtService;
import com.inhouse.project.security.UserDetailsImpl;
import com.inhouse.project.service.UsuarioService;
import com.inhouse.project.service.dto.AuthResponseDTO;
import com.inhouse.project.service.dto.LoginDTO;
import com.inhouse.project.service.dto.UsuarioRegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuariosRepository;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Usuarios> register(@RequestBody UsuarioRegisterDTO dto) {
        Usuarios usuario = usuarioService.register(dto);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.email(),
                        loginDTO.password()
                )
        );

        // Recuperando o usuário após autenticação
        Usuarios usuario = usuariosRepository.findByEmail(loginDTO.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);

        AuthResponseDTO response = new AuthResponseDTO(
                jwtToken,
                usuario.getEmail(),
                usuario.getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}