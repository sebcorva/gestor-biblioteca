package com.duoc.gestor_biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.duoc.gestor_biblioteca.model.Genero;

import jakarta.validation.constraints.Size;

public record PublicacionRequest(
        @NotBlank(message = "El titulo no puede estar vacio") @Size(max = 100, message = "El titulo no puede superar los 100 caracteres") String titulo,
        @NotBlank(message = "El autor no puede estar vacio") @Size(max = 100, message = "El autor no puede superar los 100 caracteres") String autor,
        @NotNull(message = "El año de publicacion no puede estar vacio") @Pattern(regexp = "\\d{1,4}", message = "El año debe tener entre 1 y 4 dígitos") String anioPublicacionLibro,
        @NotNull(message = "El genero no puede estar vacio") Genero genero,
        @NotBlank(message = "La reseña no puede estar vacia") @Size(min = 50, max = 1000, message = "La reseña debe tener entre 50 y 1000 caracteres") String resena) {

}
