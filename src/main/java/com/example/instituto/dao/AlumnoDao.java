package com.example.instituto.dao;

import java.util.List;

import com.example.instituto.model.Alumno;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Operaciones CRUD sobre Alumno usando únicamente la API estándar de JPA.
 *
 * Patrón seguido en cada método:
 *   1. Abrir un EntityManager (try-with-resources lo cierra al terminar).
 *   2. Las operaciones que MODIFICAN datos van dentro de una transacción.
 *   3. Las consultas de solo lectura no necesitan transacción.
 */
public class AlumnoDao {

    private final EntityManagerFactory emf;

    public AlumnoDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** CREATE: guarda un alumno nuevo y devuelve el id generado. */
    public Integer insertar(Alumno alumno) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(alumno);
                tx.commit();
                return alumno.getId();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    /** READ: busca un alumno por su clave primaria (null si no existe). */
    public Alumno buscarPorId(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Alumno.class, id);
        }
    }

    /** READ: lista todos los alumnos mediante una consulta JPQL. */
    public List<Alumno> listarTodos() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT a FROM Alumno a ORDER BY a.apellidos, a.nombre", Alumno.class)
                     .getResultList();
        }
    }

    /**
     * READ: alumnos matriculados en un curso en un curso académico.
     * Se consulta a través de la entidad Matricula; "m.curso.codigo" navega por la
     * relación @ManyToOne y JPA genera los JOIN necesarios.
     */
    public List<Alumno> listarPorCurso(String codigoCurso, String cursoAcademico) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT a FROM Matricula m JOIN m.alumno a
                    WHERE m.curso.codigo = :curso AND m.cursoAcademico = :anio
                    ORDER BY a.apellidos, a.nombre""", Alumno.class)
                     .setParameter("curso", codigoCurso)
                     .setParameter("anio", cursoAcademico)
                     .getResultList();
        }
    }

    /**
     * UPDATE: modifica los datos de un alumno existente.
     * Basta con cambiar el objeto gestionado (obtenido con find) dentro de la
     * transacción: al hacer commit, JPA detecta los cambios y lanza el UPDATE.
     */
    public boolean actualizar(int id, String nombre, String apellidos) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Alumno alumno = em.find(Alumno.class, id);
                if (alumno == null) {
                    tx.rollback();
                    return false;
                }
                alumno.setNombre(nombre);
                alumno.setApellidos(apellidos);
                tx.commit();
                return true;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    /**
     * DELETE: elimina un alumno. El objeto debe estar gestionado, por eso se busca antes.
     * Gracias a cascade = REMOVE en Alumno.matriculas, se borran también sus matrículas.
     */
    public boolean borrar(int id) {
        try (EntityManager em = emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Alumno alumno = em.find(Alumno.class, id);
                if (alumno == null) {
                    tx.rollback();
                    return false;
                }
                em.remove(alumno);
                tx.commit();
                return true;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }
}
