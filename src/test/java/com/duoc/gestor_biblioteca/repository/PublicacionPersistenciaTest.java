package com.duoc.gestor_biblioteca.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.gestor_biblioteca.model.Comentario;
import com.duoc.gestor_biblioteca.model.Genero;
import com.duoc.gestor_biblioteca.model.Publicacion;

import jakarta.persistence.EntityManager;

// Consultas y cascada reales contra H2 (perfil test).
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PublicacionPersistenciaTest {

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void buscaPorTituloOAutorSinDistinguirMayusculas() {
        guardar("Fundación", "Isaac Asimov");
        guardar("El túnel", "Ernesto Sabato");

        assertThat(publicacionRepository.buscar("ASIMOV")).extracting(Publicacion::getTitulo)
                .containsExactly("Fundación");
        assertThat(publicacionRepository.buscar("túnel")).hasSize(1);
        assertThat(publicacionRepository.buscar("nada")).isEmpty();
    }

    @Test
    void eliminarPublicacionEliminaSusComentarios() {
        Publicacion publicacion = guardar("Fundación", "Isaac Asimov");
        Comentario comentario = new Comentario();
        comentario.setContenido("Muy bueno");
        comentario.setUsuarioId(2L);
        comentario.setUsuarioNombre("Lector");
        comentario.setPublicacion(publicacion);
        publicacion.getComentarios().add(comentario);
        entityManager.flush();
        assertThat(comentarioRepository.count()).isEqualTo(1);

        publicacionRepository.delete(publicacion);
        entityManager.flush();

        assertThat(comentarioRepository.count()).isZero();
    }

    private Publicacion guardar(String titulo, String autor) {
        Publicacion p = new Publicacion();
        p.setTitulo(titulo);
        p.setAutor(autor);
        p.setAnioPublicacionLibro("1951");
        p.setGenero(Genero.CIENCIA_FICCION);
        p.setResena("Reseña de prueba");
        p.setUsuarioId(1L);
        p.setUsuarioNombre("Autor");
        return publicacionRepository.save(p);
    }
}
