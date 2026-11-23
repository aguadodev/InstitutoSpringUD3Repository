package com.example.instituto.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Matrícula de un alumno en un curso académico (p. ej. 2026-2027).
 *
 * Recoge el curso en el que se matricula (DAM2) y los módulos concretos que cursa,
 * que pueden ser solo algunos del curso o incluir módulos pendientes de otro curso
 * del mismo ciclo (p. ej. DAM2 + "Bases de datos" de DAM1).
 *
 * Un alumno solo puede tener una matrícula por curso académico (restricción UNIQUE).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"alumno_id", "cursoAcademico"}))
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)            // obligatoria: NOT NULL en la columna alumno_id
    private Alumno alumno;

    @Column(nullable = false, length = 9)
    private String cursoAcademico;          // "2026-2027"

    @ManyToOne(optional = false)
    private Curso curso;                    // curso principal en el que se matricula

    private LocalDate fecha;                // JPA convierte LocalDate a tipo fecha de la BD

    @ManyToMany
    @JoinTable(name = "Matricula_Modulo",
               joinColumns = @JoinColumn(name = "matricula_id"),
               inverseJoinColumns = @JoinColumn(name = "modulo_codigo"))
    @OrderBy("codigo")
    private List<Modulo> modulos = new ArrayList<>();

    public Matricula() {
    }

    public Matricula(Alumno alumno, String cursoAcademico, Curso curso, LocalDate fecha) {
        this.alumno = alumno;
        this.cursoAcademico = cursoAcademico;
        this.curso = curso;
        this.fecha = fecha;
    }

    public Integer getId() { return id; }
    public Alumno getAlumno() { return alumno; }
    public String getCursoAcademico() { return cursoAcademico; }
    public Curso getCurso() { return curso; }
    public LocalDate getFecha() { return fecha; }
    public List<Modulo> getModulos() { return modulos; }

    /** Suma de horas de los módulos matriculados. */
    public int getHorasTotales() {
        return modulos.stream().mapToInt(Modulo::getHoras).sum();
    }

    @Override
    public String toString() {   // no incluye "modulos" (LAZY) para no forzar su carga
        return "Matrícula " + id + ": " + alumno.getNombre() + " " + alumno.getApellidos()
                + " - " + curso.getCodigo() + " (" + cursoAcademico + ")";
    }
}
