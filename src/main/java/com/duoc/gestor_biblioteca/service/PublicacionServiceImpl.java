package com.duoc.gestor_biblioteca.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.duoc.gestor_biblioteca.dto.PublicacionRequest;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;
import com.duoc.gestor_biblioteca.exception.RecursoNoEncontradoException;
import com.duoc.gestor_biblioteca.model.Publicacion;
import com.duoc.gestor_biblioteca.repository.PublicacionRepository;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicacionServiceImpl implements PublicacionService {

    private final PublicacionRepository publicacionRepository;

    @Override
    public List<PublicacionResponse> getAllPublicaciones(String q) {
        List<Publicacion> publicaciones = (q == null || q.isBlank())
                ? publicacionRepository.findAllByOrderByFechaPublicacionDesc()
                : publicacionRepository.buscar(q.trim());

        return publicaciones.stream()
                .map(PublicacionResponse::from)
                .toList();
    }

    @Override
    public PublicacionResponse getPublicacionById(Long id) {
        return PublicacionResponse.from(buscarPorId(id));
    }

    @Override
    public PublicacionResponse createPublicacion(PublicacionRequest publicacionRequest, Long usuarioId,
            String usuarioNombre) {
        Publicacion publicacion = new Publicacion();
        copiarCampos(publicacionRequest, publicacion);
        // El autor del post viene del token (#12), nunca del body
        publicacion.setUsuarioId(usuarioId);
        publicacion.setUsuarioNombre(usuarioNombre);
        // id y fechaPublicacion no se setean: los generan la BD y @PrePersist

        return PublicacionResponse.from(publicacionRepository.save(publicacion));
    }

    @Override
    public PublicacionResponse updatePublicacion(Long id, PublicacionRequest publicacionRequest,
            UsuarioAutenticado usuario) {

        Publicacion publicacion = buscarPorId(id);
        if (!esDueno(publicacion, usuario)) {
            throw new AccessDeniedException("Solo el autor puede editar esta publicacion");
        }
        copiarCampos(publicacionRequest, publicacion);

        return PublicacionResponse.from(publicacionRepository.save(publicacion));
    }

    @Override
    public void deletePublicacion(Long id, UsuarioAutenticado usuario) {
        Publicacion publicacion = buscarPorId(id);
        if (!esDueno(publicacion, usuario) && !usuario.esAdmin()) {
            throw new AccessDeniedException("Solo el autor o un administrador pueden eliminar esta publicacion");
        }
        publicacionRepository.delete(publicacion);
    }

    private boolean esDueno(Publicacion publicacion, UsuarioAutenticado usuario) {
        return publicacion.getUsuarioId().equals(usuario.id());
    }

    private Publicacion buscarPorId(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la publicacion con id " + id));
    }

    private void copiarCampos(PublicacionRequest request, Publicacion publicacion) {
        publicacion.setTitulo(request.titulo());
        publicacion.setAutor(request.autor());
        publicacion.setAnioPublicacionLibro(request.anioPublicacionLibro());
        publicacion.setGenero(request.genero());
        publicacion.setResena(request.resena());
    }

}
