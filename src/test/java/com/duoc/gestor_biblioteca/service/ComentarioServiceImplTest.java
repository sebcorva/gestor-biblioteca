package com.duoc.gestor_biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.duoc.gestor_biblioteca.dto.ComentarioRequest;
import com.duoc.gestor_biblioteca.dto.ComentarioResponse;
import com.duoc.gestor_biblioteca.exception.RecursoNoEncontradoException;
import com.duoc.gestor_biblioteca.model.Comentario;
import com.duoc.gestor_biblioteca.model.Publicacion;
import com.duoc.gestor_biblioteca.repository.ComentarioRepository;
import com.duoc.gestor_biblioteca.repository.PublicacionRepository;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceImplTest {

    private static final UsuarioAutenticado AUTOR = new UsuarioAutenticado(1L, "a@x.cl", "Autor", "USUARIO", false);
    private static final UsuarioAutenticado OTRO = new UsuarioAutenticado(2L, "o@x.cl", "Otro", "USUARIO", true);
    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(9L, "ad@x.cl", "Admin", "ADMIN", true);

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private PublicacionRepository publicacionRepository;

    @InjectMocks
    private ComentarioServiceImpl service;

    @Test
    void listarDePublicacionInexistenteLanza404() {
        when(publicacionRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.listarPorPublicacion(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearAsignaElAutorYLaPublicacion() {
        Publicacion publicacion = new Publicacion();
        publicacion.setIdPublicacion(5L);
        when(publicacionRepository.findById(5L)).thenReturn(Optional.of(publicacion));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(inv -> inv.getArgument(0));

        ComentarioResponse creado = service.crear(5L, new ComentarioRequest("Gran libro"), 1L, "Autor");

        assertThat(creado.publicacionId()).isEqualTo(5L);
        assertThat(creado.usuarioId()).isEqualTo(1L);
        assertThat(creado.contenido()).isEqualTo("Gran libro");
    }

    @Test
    void crearEnPublicacionInexistenteLanza404() {
        when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(99L, new ComentarioRequest("x"), 1L, "Autor"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarPermitidoAlAutorYAlAdmin() {
        Comentario comentario = comentarioDe(1L);
        when(comentarioRepository.findById(3L)).thenReturn(Optional.of(comentario));

        service.eliminar(3L, AUTOR);
        service.eliminar(3L, ADMIN);

        verify(comentarioRepository, org.mockito.Mockito.times(2)).delete(comentario);
    }

    @Test
    void eliminarAjenoSinSerAdminLanza403() {
        when(comentarioRepository.findById(3L)).thenReturn(Optional.of(comentarioDe(1L)));

        assertThatThrownBy(() -> service.eliminar(3L, OTRO)).isInstanceOf(AccessDeniedException.class);
        verify(comentarioRepository, never()).delete(any());
    }

    private static Comentario comentarioDe(Long usuarioId) {
        Comentario c = new Comentario();
        c.setIdComentario(3L);
        c.setUsuarioId(usuarioId);
        c.setUsuarioNombre("Autor");
        c.setContenido("Comentario");
        return c;
    }
}
