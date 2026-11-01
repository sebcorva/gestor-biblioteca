package com.duoc.gestor_biblioteca.service;

import java.util.List;

import com.duoc.gestor_biblioteca.dto.PublicacionRequest;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;

public interface PublicacionService {

    List<PublicacionResponse> getAllPublicaciones();

    PublicacionResponse getPublicacionById(Long id);

    PublicacionResponse createPublicacion(PublicacionRequest publicacionRequest, Long usuarioId, String usuarioNombre);

    PublicacionResponse updatePublicacion(Long id, PublicacionRequest publicacionRequest);

    void deletePublicacion(Long id);

}
