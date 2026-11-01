package com.duoc.gestor_biblioteca.service;

import java.util.List;

import com.duoc.gestor_biblioteca.dto.ComentarioRequest;
import com.duoc.gestor_biblioteca.dto.ComentarioResponse;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;

public interface ComentarioService {

    List<ComentarioResponse> listarPorPublicacion(Long publicacionId);

    ComentarioResponse crear(Long publicacionId, ComentarioRequest comentarioRequest, Long usuarioId,
            String usuarioNombre);

    void eliminar(Long id, UsuarioAutenticado usuario);

}
