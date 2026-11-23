package com.example.instituto.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;

/**
 * Curso de un ciclo (p. ej. DAM2 = 2º de DAM).
 *
 * Relaciones:
 *  - Muchos cursos pertenecen a un ciclo          → @ManyToOne  (columna ciclo_codigo)
 *  - Un curso tiene muchos módulos y un módulo
 *    puede estar en muchos cursos                 → @ManyToMany (tabla intermedia Curso_Modulo)
 */
@Entity
public class Curso {

    @Id
    private String codigo;      // DAM2
    private int numero;         // 2

    @ManyToOne
    private Ciclo ciclo;

    @ManyToMany   // por defecto LAZY: los módulos no se cargan hasta que se piden
    @JoinTable(name = "Curso_Modulo",
               joinColumns = @JoinColumn(name = "curso_codigo"),
               inverseJoinColumns = @JoinColumn(name = "modulo_codigo"))
    @OrderBy("nombre")
    private List<Modulo> modulos = new ArrayList<>();

    public Curso() {
    }

    public Curso(String codigo, int numero, Ciclo ciclo) {
        this.codigo = codigo;
        this.numero = numero;
        this.ciclo = ciclo;
    }

    public String getCodigo() { return codigo; }
    public int getNumero() { return numero; }
    public Ciclo getCiclo() { return ciclo; }
    public List<Modulo> getModulos() { return modulos; }

    /** Suma de horas de los módulos del curso. */
    public int getHorasTotales() {
        return modulos.stream().mapToInt(Modulo::getHoras).sum();
    }

    @Override
    public String toString() {   // no incluye "modulos" para no forzar su carga
        return codigo + " (" + numero + "º " + ciclo.getNombre() + ")";
    }
}
