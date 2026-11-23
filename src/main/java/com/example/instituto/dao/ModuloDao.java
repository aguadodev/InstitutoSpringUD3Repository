package com.example.instituto.dao;

import java.util.List;

import com.example.instituto.model.Curso;
import com.example.instituto.model.Modulo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/** Consultas sobre módulos y su relación muchos-a-muchos con los cursos. */
public class ModuloDao {

    private final EntityManagerFactory emf;

    public ModuloDao(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Modulo buscarPorCodigo(String codigo) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Modulo.class, codigo);
        }
    }

    /** Cursos en los que se imparte un módulo: JOIN sobre la colección c.modulos. */
    public List<Curso> cursosDondeSeImparte(String codigoModulo) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT c FROM Curso c JOIN c.modulos m
                    WHERE m.codigo = :codigo
                    ORDER BY c.codigo""", Curso.class)
                     .setParameter("codigo", codigoModulo)
                     .getResultList();
        }
    }

    /** Módulos comunes a más de un curso: subconsulta con MEMBER OF. */
    public List<Modulo> listarCompartidos() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT m FROM Modulo m
                    WHERE (SELECT COUNT(c) FROM Curso c WHERE m MEMBER OF c.modulos) > 1
                    ORDER BY m.codigo""", Modulo.class)
                     .getResultList();
        }
    }
}
