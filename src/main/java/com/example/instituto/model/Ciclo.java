package com.example.instituto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Ciclo formativo (p. ej. D3IFC000200 - Desenvolvemento de aplicacións multiplataforma).
 * La clave primaria es "natural": el código oficial del ciclo, sin @GeneratedValue.
 */
@Entity
public class Ciclo {

    @Id
    private String codigo;      // D3IFC000200
    private String siglas;      // DAM
    private String nombre;      // Desenvolvemento de aplicacións multiplataforma
    private String familia;     // Informática e comunicacións
    private String grado;       // superior
    private int horas;          // 2000

    public Ciclo() {
    }

    public Ciclo(String codigo, String siglas, String nombre, String familia, String grado, int horas) {
        this.codigo = codigo;
        this.siglas = siglas;
        this.nombre = nombre;
        this.familia = familia;
        this.grado = grado;
        this.horas = horas;
    }

    public String getCodigo() { return codigo; }
    public String getSiglas() { return siglas; }
    public String getNombre() { return nombre; }
    public String getFamilia() { return familia; }
    public String getGrado() { return grado; }
    public int getHoras() { return horas; }

    @Override
    public String toString() {
        return siglas + " - " + nombre + " (" + codigo + ", grado " + grado + ", " + horas + " h)";
    }
}
