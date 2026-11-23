package com.example.instituto.repository;

import com.example.instituto.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    // Alumnos matriculados en un curso en un curso académico (a través de Matricula)
    @Query("""
            SELECT a FROM Matricula m JOIN m.alumno a
            WHERE m.curso.codigo = :curso AND m.cursoAcademico = :anio
            ORDER BY a.apellidos, a.nombre""")
    List<Alumno> findMatriculados(@Param("curso") String codigoCurso, @Param("anio") String cursoAcademico);

    // Si prefieres JPQL explícito para el listado ordenado
    @Query("SELECT a FROM Alumno a ORDER BY a.apellidos, a.nombre")
    List<Alumno> findAllOrdenados();

    // Consulta con LIKE (ejercicio 2 del README)
    @Query("SELECT a FROM Alumno a WHERE a.nombre LIKE %:texto%")
    List<Alumno> findByNombreContiene(@Param("texto") String texto);

    // Agregación: contar alumnos matriculados por curso en un curso académico (ejercicio 3)
    @Query("""
            SELECT m.curso.codigo, COUNT(m) FROM Matricula m
            WHERE m.cursoAcademico = :anio
            GROUP BY m.curso.codigo
            ORDER BY m.curso.codigo""")
    List<Object[]> contarPorCurso(@Param("anio") String cursoAcademico);
}