package com.duoc.gestor_biblioteca.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.duoc.gestor_biblioteca.dto.ComentarioRequest;
import com.duoc.gestor_biblioteca.dto.ComentarioResponse;
import com.duoc.gestor_biblioteca.exception.RecursoNoEncontradoException;
import com.duoc.gestor_biblioteca.model.Comentario;
import com.duoc.gestor_biblioteca.model.Publicacion;
import com.duoc.gestor_biblioteca.repository.ComentarioRepository;
import com.duoc.gestor_biblioteca.repository.PublicacionRepository;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComentarioServiceImpl implements ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;

    @Override
    public List<ComentarioResponse> listarPorPublicacion(Long publicacionId) {
        if (!publicacionRepository.existsById(publicacionId)) {
            throw new RecursoNoEncontradoException("No existe la publicacion con id " + publicacionId);
        }

        return comentarioRepository.findByPublicacion_IdPublicacionOrderByFechaAsc(publicacionId)
                .stream()
                .map(ComentarioResponse::from)
                .toList();
    }

    @Override
    public ComentarioResponse crear(Long publicacionId, ComentarioRequest comentarioRequest, Long usuarioId,
            String usuarioNombre) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la publicacion con id " + publicacionId));

        Comentario comentario = new Comentario();
        comentario.setContenido(comentarioRequest.contenido());
        // usuarioId y usuarioNombre vienen del token
        comentario.setUsuarioId(usuarioId);
        comentario.setUsuarioNombre(usuarioNombre);
        comentario.setPublicacion(publicacion);

        return ComentarioResponse.from(comentarioRepository.save(comentario));
    }

    @Override
    public void eliminar(Long id, UsuarioAutenticado usuario) {
        // 1. Existe? (404)  2. Es el autor o ADMIN? (403)
        // findById y no existsById: se necesita el objeto para saber quien es el autor
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el comentario con id " + id));

        boolean esAutor = comentario.getUsuarioId().equals(usuario.id());
        if (!esAutor && !usuario.esAdmin()) {
            throw new AccessDeniedException("Solo el autor o un administrador pueden eliminar este comentario");
        }
        comentarioRepository.delete(comentario);
    }
}
