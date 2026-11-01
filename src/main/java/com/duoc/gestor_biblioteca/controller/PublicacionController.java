package com.duoc.gestor_biblioteca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.gestor_biblioteca.dto.PublicacionRequest;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;
import com.duoc.gestor_biblioteca.service.PublicacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    @GetMapping
    public ResponseEntity<List<PublicacionResponse>> getAllPublicaciones(
            @RequestParam(required = false) String q) {
        List<PublicacionResponse> publicaciones = publicacionService.getAllPublicaciones(q);
        return ResponseEntity.ok(publicaciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> getPublicacionById(@PathVariable Long id) {
        PublicacionResponse publicacion = publicacionService.getPublicacionById(id);
        return ResponseEntity.ok(publicacion);
    }

    // Solo usuarios premium o ADMIN pueden publicar
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PREMIUM')")
    public ResponseEntity<PublicacionResponse> createPublicacion(
            @Valid @RequestBody PublicacionRequest publicacionRequest,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        // El autor se toma del token
        PublicacionResponse publicacionCreada = publicacionService.createPublicacion(publicacionRequest,
                usuario.id(), usuario.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionCreada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionResponse> updatePublicacion(@PathVariable Long id,
            @Valid @RequestBody PublicacionRequest publicacionRequest,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        PublicacionResponse publicacionActualizada = publicacionService.updatePublicacion(id, publicacionRequest,
                usuario);
        return ResponseEntity.ok(publicacionActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublicacion(@PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        publicacionService.deletePublicacion(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
