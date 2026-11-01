package com.duoc.gestor_biblioteca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.gestor_biblioteca.dto.ComentarioRequest;
import com.duoc.gestor_biblioteca.dto.ComentarioResponse;
import com.duoc.gestor_biblioteca.service.ComentarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Sin @RequestMapping en la clase: las rutas tienen prefijos distintos
// (anidadas bajo /publicaciones y planas bajo /comentarios)
@RestController
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> listarPorPublicacion(@PathVariable Long publicacionId) {
        List<ComentarioResponse> comentarios = comentarioService.listarPorPublicacion(publicacionId);
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping("/publicaciones/{publicacionId}/comentarios")
    public ResponseEntity<ComentarioResponse> crear(@PathVariable Long publicacionId,
            @Valid @RequestBody ComentarioRequest comentarioRequest) {
        // TODO #12: tomar id y nombre del usuario desde el token
        ComentarioResponse comentarioCreado = comentarioService.crear(publicacionId, comentarioRequest, 1L,
                "Usuario Demo");
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioCreado);
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        comentarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

}
