package com.inhouse.project.resource;

import com.inhouse.project.domain.Usuarios;
import com.inhouse.project.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioResource {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    @Operation(summary = "Retorna informações do usuário logado")
    @ApiResponse(responseCode = "200", description = "Informações do usuário retornadas com sucesso")
    @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Usuarios usuario = usuarioService.getUsuarioLogado();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", usuario.getId());
        userInfo.put("nome", usuario.getNome());
        userInfo.put("email", usuario.getEmail());
        userInfo.put("role", usuario.getRole());
        
        if (usuario.getRole() == Usuarios.Role.PROPRIETARIO) {
            try {
                // Tenta obter o ID do proprietário associado ao usuário
                Long proprietarioId = usuarioService
                    .getProprietarioDoUsuario(usuario.getId())
                    .getId();
                userInfo.put("proprietarioId", proprietarioId);
            } catch (Exception e) {
                log.warn("Erro ao buscar proprietário para usuário {}: {}", usuario.getId(), e.getMessage());
                userInfo.put("proprietarioId", null);
            }
        }
        
        log.info("Usuário {} (ID: {}, Role: {}) solicitou suas informações", 
                usuario.getEmail(), usuario.getId(), usuario.getRole());
        
        return ResponseEntity.ok(userInfo);
    }
}
