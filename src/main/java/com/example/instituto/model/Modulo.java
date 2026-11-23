package com.example.instituto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Módulo profesional (p. ej. MP0486 - Acceso a datos).
 * Un mismo módulo puede impartirse en varios cursos (ver Curso.modulos).
 */
@Entity
public class Modulo {

    @Id
    private String codigo;          // MP0486
    private String nombre;          // Acceso a datos
    private int horas;              // 220
    private int periodosSemanales;  // 8

    public Modulo() {
    }

    public Modulo(String codigo, String nombre, int horas, int periodosSemanales) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.horas = horas;
        this.periodosSemanales = periodosSemanales;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getHoras() { return horas; }
    public int getPeriodosSemanales() { return periodosSemanales; }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + horas + " h, " + periodosSemanales + " sesiones/semana)";
    }
}
