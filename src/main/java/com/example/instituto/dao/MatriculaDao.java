package com.example.instituto.dao;

import java.time.LocalDate;
import java.util.List;

import com.example.instituto.model.Alumno;
import com.example.instituto.model.Curso;
import com.example.instituto.model.Matricula;
import com.example.instituto.model.Modulo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/** Operaciones sobre matrículas usando la API estándar de JPA. */
public class MatriculaDao {

    private final EntityManagerFactory emf;

    public MatriculaDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * CREATE: matricula a un alumno en un curso académico.
     *
     * @param codigosModulos módulos que cursa; si está vacía, todos los del curso.
     *                       Pueden ser de cualquier curso del mismo ciclo (pendientes).
     * @return id de la nueva matrícula
     * @throws IllegalArgumentException si algún dato no es válido (se hace rollback)
     */
    public Integer matricular(int alumnoId, String cursoAcademico, String codigoCurso, List<String> codigosModulos) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                Alumno alumno = em.find(Alumno.class, alumnoId);
                if (alumno == null) {
                    throw new IllegalArgumentException("No existe el alumno " + alumnoId);
                }
                Curso curso = em.find(Curso.class, codigoCurso);
                if (curso == null) {
                    throw new IllegalArgumentException("No existe el curso " + codigoCurso);
                }
                long previas = em.createQuery("""
                        SELECT COUNT(m) FROM Matricula m
                        WHERE m.alumno = :alumno AND m.cursoAcademico = :anio""", Long.class)
                        .setParameter("alumno", alumno)     // se puede comparar con la entidad
                        .setParameter("anio", cursoAcademico)
                        .getSingleResult();
                if (previas > 0) {
                    throw new IllegalArgumentException("El alumno ya tiene matrícula en " + cursoAcademico);
                }

                Matricula matricula = new Matricula(alumno, cursoAcademico, curso, LocalDate.now());
                if (codigosModulos.isEmpty()) {
                    matricula.getModulos().addAll(curso.getModulos());   // curso completo
                } else {
                    matricula.getModulos().addAll(modulosDelCiclo(em, curso, codigosModulos));
                }

                em.persist(matricula);
                tx.commit();
                return matricula.getId();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    /** Busca los módulos indicados comprobando que pertenecen a algún curso del ciclo. */
    private List<Modulo> modulosDelCiclo(EntityManager em, Curso curso, List<String> codigos) {
        List<Modulo> modulos = em.createQuery("""
                SELECT DISTINCT mo FROM Curso c JOIN c.modulos mo
                WHERE c.ciclo = :ciclo AND mo.codigo IN :codigos""", Modulo.class)
                .setParameter("ciclo", curso.getCiclo())
                .setParameter("codigos", codigos)            // IN admite una colección
                .getResultList();
        if (modulos.size() != codigos.size()) {
            throw new IllegalArgumentException("Hay módulos que no existen o no son del ciclo "
                    + curso.getCiclo().getSiglas());
        }
        return modulos;
    }

    /** READ: matrículas de un alumno con sus módulos (JOIN FETCH, la colección es LAZY). */
    public List<Matricula> listarDeAlumno(int alumnoId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT DISTINCT m FROM Matricula m LEFT JOIN FETCH m.modulos
                    WHERE m.alumno.id = :id
                    ORDER BY m.cursoAcademico""", Matricula.class)
                     .setParameter("id", alumnoId)
                     .getResultList();
        }
    }

    /** READ: alumnos matriculados en un módulo en un curso académico (JOIN sobre la colección). */
    public List<Alumno> alumnosDeModulo(String codigoModulo, String cursoAcademico) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT a FROM Matricula m JOIN m.modulos mo JOIN m.alumno a
                    WHERE mo.codigo = :modulo AND m.cursoAcademico = :anio
                    ORDER BY a.apellidos, a.nombre""", Alumno.class)
                     .setParameter("modulo", codigoModulo)
                     .setParameter("anio", cursoAcademico)
                     .getResultList();
        }
    }

    /** DELETE: anula una matrícula (se borran también sus filas de Matricula_Modulo). */
    public boolean anular(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Matricula matricula = em.find(Matricula.class, id);
                if (matricula == null) {
                    tx.rollback();
                    return false;
                }
                em.remove(matricula);
                tx.commit();
                return true;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    /** Curso académico de una fecha: de septiembre a agosto (17/11/2026 → "2026-2027"). */
    public static String cursoAcademicoDe(LocalDate fecha) {
        int inicio = fecha.getMonthValue() >= 9 ? fecha.getYear() : fecha.getYear() - 1;
        return inicio + "-" + (inicio + 1);
    }
}
