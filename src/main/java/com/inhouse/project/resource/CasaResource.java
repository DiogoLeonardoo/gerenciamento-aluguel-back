package com.inhouse.project.resource;

import com.inhouse.project.domain.Casa;
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

@RestController
@RequestMapping("/api/casas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
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
}
