package com.duoc.gestor_biblioteca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.gestor_biblioteca.dto.PublicacionRequest;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;
import com.duoc.gestor_biblioteca.service.PublicacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;

    @GetMapping
    public ResponseEntity<List<PublicacionResponse>> getAllPublicaciones() {
        List<PublicacionResponse> publicacion = publicacionService.getAllPublicaciones();
        return ResponseEntity.ok(publicacion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> getPublicacionById(@PathVariable Long id) {
        PublicacionResponse publicacion = publicacionService.getPublicacionById(id);
        return ResponseEntity.ok(publicacion);
    }

    @PostMapping
    public ResponseEntity<PublicacionResponse> createPublicacion(
            @Valid @RequestBody PublicacionRequest publicacionRequest) {
        PublicacionResponse publicacionCreada = publicacionService.createPublicacion(publicacionRequest, 1L,
                "Usuario_Demo");
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionCreada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionResponse> updatePublicacion(@PathVariable Long id,
            @Valid @RequestBody PublicacionRequest publicacionRequest) {
        PublicacionResponse publicacionActualizada = publicacionService.updatePublicacion(id, publicacionRequest);
        return ResponseEntity.ok(publicacionActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublicacion(@PathVariable Long id) {
        publicacionService.deletePublicacion(id);
        return ResponseEntity.noContent().build();
    }
}
