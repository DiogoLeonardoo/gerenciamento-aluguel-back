package com.inhouse.project.resource;

import com.inhouse.project.domain.Casa;
import com.inhouse.project.domain.Proprietario;
import com.inhouse.project.domain.Usuarios;
import com.inhouse.project.exceptions.BusinessException;
import com.inhouse.project.service.CasaService;
import com.inhouse.project.service.UsuarioService;
import com.inhouse.project.service.dto.CasaCreateDTO;
import com.inhouse.project.service.dto.CasaJsonDTO;
import com.inhouse.project.service.dto.CasaUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/casas")
@RequiredArgsConstructor
public class CasaResource {

    private final CasaService casaService;
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Casa>> listarCasas() {
        List<Casa> casas;

        // Verifica o usuário logado
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();

        if (usuarioLogado.getRole() == Usuarios.Role.ADMIN) {
            casas = casaService.listarCasasAtivas();
        } else if (usuarioLogado.getRole() == Usuarios.Role.PROPRIETARIO) {
            casas = casaService.listarCasasDoProprietario();
        } else {
            casas = List.of();
        }

        return ResponseEntity.ok(casas);
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Casa> criarCasa(@ModelAttribute CasaCreateDTO dto) {
        Casa casaSalva = casaService.salvarCasa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(casaSalva);
    }
    
    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Casa> criarCasa(@RequestBody CasaJsonDTO dto) {
        if (dto.getBanheiros() == null) {
            throw new BusinessException("Número de banheiros é obrigatório");
        }
        if (dto.getQuartos() == null) {
            throw new BusinessException("Número de quartos é obrigatório");
        }
        if (dto.getMaxPessoas() == null) {
            throw new BusinessException("Capacidade máxima é obrigatória");
        }
        if (dto.getValorDiaria() == null) {
            throw new BusinessException("Valor da diária é obrigatório");
        }
        if (dto.getEstado() != null && dto.getEstado().length() > 2) {
            throw new BusinessException("Estado deve ter no máximo 2 caracteres (sigla do estado)");
        }
        
        CasaCreateDTO casaDTO = dto.toCasaCreateDTO();
        Casa casaSalva = casaService.salvarCasa(casaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(casaSalva);
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @PatchMapping("/{id}")
    public ResponseEntity<Casa> atualizarParcial(
            @PathVariable Long id,
            @ModelAttribute CasaUpdateDTO dto
    ) {
        Casa casaAtualizada = casaService.atualizarParcial(id, dto);
        return ResponseEntity.ok(casaAtualizada);
    }


    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Busca uma casa pelo ID", 
        description = "Retorna os detalhes de uma casa específica. Proprietários só podem ver suas próprias casas, " +
                      "administradores podem ver todas as casas, e outros usuários só podem ver casas ativas.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Casa encontrada com sucesso")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuário não tem permissão para ver esta casa")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Casa não encontrada")
    public ResponseEntity<Casa> buscarCasaPorId(@PathVariable Long id) {
        try {
            // Obtém a casa pelo ID
            Casa casa = casaService.buscarPorId(id);
            
            // Verifica se o usuário logado tem permissão para ver esta casa
            Usuarios usuarioLogado = usuarioService.getUsuarioLogado();
            
            // Administradores podem ver qualquer casa
            if (usuarioLogado.getRole() == Usuarios.Role.ADMIN) {
                return ResponseEntity.ok(casa);
            }
            
            // Proprietários só podem ver suas próprias casas
            if (usuarioLogado.getRole() == Usuarios.Role.PROPRIETARIO) {
                Proprietario proprietario = usuarioService.getProprietarioDoUsuario(usuarioLogado.getId());
                // Verificar se a casa pertence ao proprietário atual
                if (casa.getProprietario() != null && 
                    casa.getProprietario().getId().equals(proprietario.getId())) {
                    return ResponseEntity.ok(casa);
                }
            }
            
            // Verificar se a casa está ativa (visível para todos)
            if (Boolean.TRUE.equals(casa.getAtiva())) {
                return ResponseEntity.ok(casa);
            }
            
            // Se chegou aqui, o usuário não tem permissão
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(null);
        }
    }
    
    @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativarCasa(@PathVariable Long id) {
        casaService.inativar(id);
        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativarCasaPorPatch(@PathVariable Long id) {
        // Endpoint alternativo usando PATCH para inativar uma casa
        casaService.inativar(id);
        return ResponseEntity.noContent().build();
    }

     @PreAuthorize("hasRole('PROPRIETARIO') or hasRole('ADMIN')")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativarCasaPorPatch(@PathVariable Long id) {
        // Endpoint alternativo usando PATCH para ATIVAR uma casa
        casaService.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('PROPRIETARIO')")
    @GetMapping("/quantidade")
    @io.swagger.v3.oas.annotations.Operation(
        summary = "Conta a quantidade de imóveis de um proprietário",
        description = "Retorna o total de casas que pertencem ao proprietário logado"
    )
    public ResponseEntity<Map<String, Integer>> contarCasasDoProprietario() {
        // Obtém o usuário logado
        Usuarios usuarioLogado = usuarioService.getUsuarioLogado();
        
        // Certifica que é um proprietário
        if (usuarioLogado.getRole() != Usuarios.Role.PROPRIETARIO) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Obtém a lista de casas do proprietário
        List<Casa> casas = casaService.listarCasasDoProprietario();
        
        // Retorna apenas a quantidade
        Map<String, Integer> response = Map.of("quantidade", casas.size());
        return ResponseEntity.ok(response);
    }
}
