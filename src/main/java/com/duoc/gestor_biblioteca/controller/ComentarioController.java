package com.duoc.gestor_biblioteca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.gestor_biblioteca.dto.ComentarioRequest;
import com.duoc.gestor_biblioteca.dto.ComentarioResponse;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;
import com.duoc.gestor_biblioteca.service.ComentarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> listarPorPublicacion(@PathVariable Long publicacionId) {
        List<ComentarioResponse> comentarios = comentarioService.listarPorPublicacion(publicacionId);
        return ResponseEntity.ok(comentarios);
    }

    // Cualquier usuario logueado comenta
    @PostMapping("/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<ComentarioResponse> crear(@PathVariable Long publicacionId,
            @Valid @RequestBody ComentarioRequest comentarioRequest,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ComentarioResponse comentarioCreado = comentarioService.crear(publicacionId, comentarioRequest,
                usuario.id(), usuario.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioCreado);
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        comentarioService.eliminar(id, usuario);
        return ResponseEntity.noContent().build();
    }

}
