package com.duoc.gestor_biblioteca.dto;

import java.time.LocalDateTime;

import com.duoc.gestor_biblioteca.model.Genero;
import com.duoc.gestor_biblioteca.model.Publicacion;

public record PublicacionResponse(
        Long idPublicacion,
        String titulo,
        String autor,
        String anioPublicacionLibro,
        Genero genero,
        String resena,
        LocalDateTime fechaPublicacion,
        Long usuarioId,
        String usuarioNombre) {

    public static PublicacionResponse from(Publicacion publicacion) {
        return new PublicacionResponse(
                publicacion.getIdPublicacion(),
                publicacion.getTitulo(),
                publicacion.getAutor(),
                publicacion.getAnioPublicacionLibro(),
                publicacion.getGenero(),
                publicacion.getResena(),
                publicacion.getFechaPublicacion(),
                publicacion.getUsuarioId(),
                publicacion.getUsuarioNombre());
    }
}
