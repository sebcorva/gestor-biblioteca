package com.duoc.gestor_biblioteca.security;

// Datos del usuario leidos del JWT para guardar en el SecurityContext
public record UsuarioAutenticado(
        Long id,
        String email,
        String nombre,
        String rol,
        boolean premium) {

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

}
