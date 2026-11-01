package com.duoc.gestor_biblioteca.service;

import java.util.List;

import com.duoc.gestor_biblioteca.dto.PublicacionRequest;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;

public interface PublicacionService {

    List<PublicacionResponse> getAllPublicaciones(String q);

    PublicacionResponse getPublicacionById(Long id);

    PublicacionResponse createPublicacion(PublicacionRequest publicacionRequest, Long usuarioId, String usuarioNombre);

    PublicacionResponse updatePublicacion(Long id, PublicacionRequest publicacionRequest, UsuarioAutenticado usuario);

    void deletePublicacion(Long id, UsuarioAutenticado usuario);

}
