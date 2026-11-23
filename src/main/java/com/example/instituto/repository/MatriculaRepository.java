package com.example.instituto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.instituto.model.Matricula;

public interface MatriculaRepository extends JpaRepository<Matricula, Integer> {

    // Consultas derivadas: "AlumnoId" navega por la relación → WHERE m.alumno.id = ?
    List<Matricula> findByAlumnoIdOrderByCursoAcademico(Integer alumnoId);

    // "ModulosCodigo" navega por la colección → JOIN m.modulos mo WHERE mo.codigo = ?
    List<Matricula> findByModulosCodigoAndCursoAcademico(String codigoModulo, String cursoAcademico);

    boolean existsByAlumnoIdAndCursoAcademico(Integer alumnoId, String cursoAcademico);
}
