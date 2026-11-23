package com.example.instituto.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/**
 * Entidad JPA: cada objeto Alumno se corresponde con una fila de la tabla "Alumno".
 *
 * El curso en el que está un alumno NO se guarda aquí: depende del año académico y
 * lo indica cada una de sus matrículas.
 */
@Entity
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // la BD genera el id (autoincremento)
    private Integer id;

    private String nombre;
    private String apellidos;

    /**
     * Lado inverso de la relación Matricula.alumno (relación bidireccional).
     * "mappedBy" indica que la clave ajena está en la tabla Matricula, no aquí.
     * CascadeType.REMOVE: al borrar el alumno se borran también sus matrículas.
     */
    @OneToMany(mappedBy = "alumno", cascade = CascadeType.REMOVE)
    @OrderBy("cursoAcademico")
    private List<Matricula> matriculas = new ArrayList<>();

    /** Constructor vacío obligatorio para JPA. */
    public Alumno() {
    }

    public Alumno(String nombre, String apellidos) {
        this.nombre = nombre;
        this.apellidos = apellidos;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public List<Matricula> getMatriculas() {
        return matriculas;
    }

    @Override
    public String toString() {   // no incluye "matriculas" (LAZY) para no forzar su carga
        return "Alumno [id=" + id + ", nombre=" + nombre + ", apellidos=" + apellidos + "]";
    }
}
