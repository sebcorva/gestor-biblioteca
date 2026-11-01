package com.duoc.gestor_biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "El comentario no puede estar vacio") @Size(max = 500, message = "El comentario no puede superar los 500 caracteres") String contenido) {

}
