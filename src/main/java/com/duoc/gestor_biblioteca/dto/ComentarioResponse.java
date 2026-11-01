package com.duoc.gestor_biblioteca.dto;

import java.time.LocalDateTime;

import com.duoc.gestor_biblioteca.model.Comentario;

public record ComentarioResponse(
        Long idComentario,
        Long publicacionId,
        Long usuarioId,
        String usuarioNombre,
        String contenido,
        LocalDateTime fecha) {

    public static ComentarioResponse from(Comentario comentario) {
        return new ComentarioResponse(
                comentario.getIdComentario(),
                comentario.getPublicacion().getIdPublicacion(),
                comentario.getUsuarioId(),
                comentario.getUsuarioNombre(),
                comentario.getContenido(),
                comentario.getFecha());
    }
}
