package com.duoc.gestor_biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.duoc.gestor_biblioteca.dto.PublicacionRequest;
import com.duoc.gestor_biblioteca.dto.PublicacionResponse;
import com.duoc.gestor_biblioteca.exception.RecursoNoEncontradoException;
import com.duoc.gestor_biblioteca.model.Genero;
import com.duoc.gestor_biblioteca.model.Publicacion;
import com.duoc.gestor_biblioteca.repository.PublicacionRepository;
import com.duoc.gestor_biblioteca.security.UsuarioAutenticado;

@ExtendWith(MockitoExtension.class)
class PublicacionServiceImplTest {

    private static final UsuarioAutenticado AUTOR = new UsuarioAutenticado(1L, "autor@x.cl", "Autor", "USUARIO", true);
    private static final UsuarioAutenticado OTRO = new UsuarioAutenticado(2L, "otro@x.cl", "Otro", "USUARIO", true);
    private static final UsuarioAutenticado ADMIN = new UsuarioAutenticado(9L, "admin@x.cl", "Admin", "ADMIN", true);

    @Mock
    private PublicacionRepository publicacionRepository;

    @InjectMocks
    private PublicacionServiceImpl service;

    @Test
    void sinBusquedaListaTodasOrdenadas() {
        when(publicacionRepository.findAllByOrderByFechaPublicacionDesc()).thenReturn(List.of(publicacion(1L)));

        assertThat(service.getAllPublicaciones(null)).hasSize(1);
        assertThat(service.getAllPublicaciones("   ")).hasSize(1);
        verify(publicacionRepository, never()).buscar(any());
    }

    @Test
    void conBusquedaUsaElTextoSinEspacios() {
        when(publicacionRepository.buscar("asimov")).thenReturn(List.of(publicacion(1L)));

        assertThat(service.getAllPublicaciones("  asimov ")).hasSize(1);
    }

    @Test
    void detalleInexistenteLanza404() {
        when(publicacionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPublicacionById(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crearTomaElAutorDeLosParametros() {
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicacionResponse creada = service.createPublicacion(request("Fundación"), 1L, "Autor");

        assertThat(creada.usuarioId()).isEqualTo(1L);
        assertThat(creada.usuarioNombre()).isEqualTo("Autor");
        assertThat(creada.titulo()).isEqualTo("Fundación");
    }

    @Test
    void elAutorEditaYLaFechaNoCambia() {
        Publicacion existente = publicacion(1L);
        LocalDateTime fechaOriginal = existente.getFechaPublicacion();
        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(publicacionRepository.save(any(Publicacion.class))).thenAnswer(inv -> inv.getArgument(0));

        PublicacionResponse editada = service.updatePublicacion(1L, request("Nuevo título"), AUTOR);

        assertThat(editada.titulo()).isEqualTo("Nuevo título");
        assertThat(editada.fechaPublicacion()).isEqualTo(fechaOriginal);
    }

    @Test
    void niOtroUsuarioNiAdminPuedenEditarLoAjeno() {
        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion(1L)));

        assertThatThrownBy(() -> service.updatePublicacion(1L, request("x"), OTRO))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.updatePublicacion(1L, request("x"), ADMIN))
                .isInstanceOf(AccessDeniedException.class);
        verify(publicacionRepository, never()).save(any());
    }

    @Test
    void eliminarPermitidoAlAutorYAlAdmin() {
        Publicacion publicacion = publicacion(1L);
        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion));

        service.deletePublicacion(1L, AUTOR);
        service.deletePublicacion(1L, ADMIN);

        verify(publicacionRepository, org.mockito.Mockito.times(2)).delete(publicacion);
    }

    @Test
    void eliminarAjenoSinSerAdminLanza403() {
        when(publicacionRepository.findById(1L)).thenReturn(Optional.of(publicacion(1L)));

        assertThatThrownBy(() -> service.deletePublicacion(1L, OTRO))
                .isInstanceOf(AccessDeniedException.class);
        verify(publicacionRepository, never()).delete(any());
    }

    private static PublicacionRequest request(String titulo) {
        return new PublicacionRequest(titulo, "Isaac Asimov", "1951", Genero.CIENCIA_FICCION,
                "Una reseña de prueba suficientemente larga para cumplir el mínimo de cincuenta caracteres.");
    }

    private static Publicacion publicacion(Long usuarioId) {
        Publicacion p = new Publicacion();
        p.setIdPublicacion(1L);
        p.setTitulo("Fundación");
        p.setAutor("Isaac Asimov");
        p.setAnioPublicacionLibro("1951");
        p.setGenero(Genero.CIENCIA_FICCION);
        p.setResena("Reseña");
        p.setFechaPublicacion(LocalDateTime.of(2026, 1, 1, 10, 0));
        p.setUsuarioId(usuarioId);
        p.setUsuarioNombre("Autor");
        return p;
    }
}
