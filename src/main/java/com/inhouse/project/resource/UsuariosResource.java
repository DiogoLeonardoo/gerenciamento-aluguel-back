package com.inhouse.project.resource;

import com.inhouse.project.service.UsuarioService;
import com.inhouse.project.service.dto.UsuarioRegisterDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuariosResource {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioRegisterDTO> getTodosUsuarios() {
        return usuarioService.listarTodosUsuarios();
    }
}
