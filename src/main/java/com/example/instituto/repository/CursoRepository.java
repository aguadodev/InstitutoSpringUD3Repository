package com.example.instituto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.instituto.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, String> {

    // Cursos que incluyen un módulo: se navega por la colección "modulos" (Modulos_Codigo)
    List<Curso> findByModulosCodigoOrderByCodigo(String codigoModulo);

    // JOIN FETCH para traer los módulos (colección LAZY) en la misma consulta
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.modulos ORDER BY c.codigo")
    List<Curso> findAllConModulos();
}
