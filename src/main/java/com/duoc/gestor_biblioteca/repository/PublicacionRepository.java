package com.duoc.gestor_biblioteca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.duoc.gestor_biblioteca.model.Publicacion;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    // Listado sin busqueda
    List<Publicacion> findAllByOrderByFechaPublicacionDesc();

    // Busqueda libre
    @Query("SELECT p FROM Publicacion p "
            + "WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "OR LOWER(p.autor) LIKE LOWER(CONCAT('%', :q, '%')) "
            + "ORDER BY p.fechaPublicacion DESC")
    List<Publicacion> buscar(@Param("q") String q);

}
