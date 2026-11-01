package com.duoc.gestor_biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duoc.gestor_biblioteca.model.Comentario;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    List<Comentario> findByPublicacion_IdPublicacionOrderByFechaAsc(Long publicacionId);

}
