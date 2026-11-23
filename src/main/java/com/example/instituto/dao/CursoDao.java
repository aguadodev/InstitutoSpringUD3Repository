package com.example.instituto.dao;

import java.util.List;

import com.example.instituto.model.Curso;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/** Consultas sobre cursos (solo lectura: los datos vienen de la precarga). */
public class CursoDao {

    private final EntityManagerFactory emf;

    public CursoDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** Busca un curso por su clave primaria (DAM1, DAW2...). Devuelve null si no existe. */
    public Curso buscarPorCodigo(String codigo) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Curso.class, codigo);
        }
    }

    /**
     * Lista los cursos CON sus módulos.
     *
     * La colección "modulos" es LAZY: si no la cargamos aquí, al cerrar el EntityManager
     * ya no se podría recorrer (LazyInitializationException). "JOIN FETCH" indica a JPA
     * que traiga los módulos en la misma consulta.
     */
    public List<Curso> listarConModulos() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT DISTINCT c FROM Curso c
                    LEFT JOIN FETCH c.modulos
                    ORDER BY c.codigo""", Curso.class)
                     .getResultList();
        }
    }
}
