package com.example.instituto;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.example.instituto.dao.AlumnoDao;
import com.example.instituto.dao.CursoDao;
import com.example.instituto.dao.MatriculaDao;
import com.example.instituto.dao.ModuloDao;
import com.example.instituto.datos.DatosIniciales;
import com.example.instituto.model.Alumno;
import com.example.instituto.model.Curso;
import com.example.instituto.model.Matricula;
import com.example.instituto.model.Modulo;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Aplicación de consola con las operaciones CRUD básicas sobre Alumno, gestión de
 * matrículas y consultas sobre cursos y módulos, usando JPA "puro" (sin Spring), configurado en
 * META-INF/persistence.xml.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    /** Curso académico por defecto: el actual (p. ej. "2026-2027"). */
    private static final String CURSO_ACTUAL = MatriculaDao.cursoAcademicoDe(LocalDate.now());

    public static void main(String[] args) {
        // La EntityManagerFactory es costosa de crear: una sola para toda la aplicación.
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("institutoPU")) {
            DatosIniciales.cargarSiVacia(emf);

            AlumnoDao alumnoDao = new AlumnoDao(emf);
            CursoDao cursoDao = new CursoDao(emf);
            ModuloDao moduloDao = new ModuloDao(emf);
            MatriculaDao matriculaDao = new MatriculaDao(emf);

            int opcion;
            do {
                System.out.println("""

                        ===== GESTIÓN DEL INSTITUTO =====
                        -- Alumnos --
                         1. Listar alumnos
                         2. Listar alumnos matriculados en un curso
                         3. Buscar alumno por id
                         4. Nuevo alumno
                         5. Modificar alumno
                         6. Borrar alumno (y sus matrículas)
                        -- Matrículas --
                         7. Ver matrículas de un alumno
                         8. Nueva matrícula
                         9. Anular matrícula
                        10. Alumnos matriculados en un módulo
                        -- Cursos y módulos --
                        11. Listar cursos con sus módulos
                        12. Cursos en los que se imparte un módulo
                        13. Módulos comunes a varios cursos
                         0. Salir""");
                opcion = leerEntero("Opción: ");

                try {
                    switch (opcion) {
                        case 1 -> mostrar(alumnoDao.listarTodos());
                        case 2 -> mostrar(alumnoDao.listarPorCurso(
                                leerTexto("Curso (DAM1, DAW2...): ").toUpperCase(), leerCursoAcademico()));
                        case 3 -> {
                            Alumno a = alumnoDao.buscarPorId(leerEntero("Id: "));
                            System.out.println(a != null ? a : "No existe ningún alumno con ese id.");
                        }
                        case 4 -> {
                            Alumno nuevo = new Alumno(leerTexto("Nombre: "), leerTexto("Apellidos: "));
                            System.out.println("Alumno guardado con id " + alumnoDao.insertar(nuevo));
                        }
                        case 5 -> {
                            int id = leerEntero("Id del alumno a modificar: ");
                            boolean ok = alumnoDao.actualizar(id, leerTexto("Nuevo nombre: "), leerTexto("Nuevos apellidos: "));
                            System.out.println(ok ? "Alumno modificado." : "No existe ningún alumno con ese id.");
                        }
                        case 6 -> {
                            boolean ok = alumnoDao.borrar(leerEntero("Id del alumno a borrar: "));
                            System.out.println(ok ? "Alumno borrado." : "No existe ningún alumno con ese id.");
                        }
                        case 7 -> {
                            List<Matricula> matriculas = matriculaDao.listarDeAlumno(leerEntero("Id del alumno: "));
                            if (matriculas.isEmpty()) {
                                System.out.println("No tiene matrículas.");
                            }
                            for (Matricula mat : matriculas) {
                                System.out.println("\n" + mat + " - " + mat.getHorasTotales() + " h");
                                mat.getModulos().forEach(mod -> System.out.println("   " + mod));
                            }
                        }
                        case 8 -> nuevaMatricula(matriculaDao, cursoDao);
                        case 9 -> {
                            boolean ok = matriculaDao.anular(leerEntero("Id de la matrícula: "));
                            System.out.println(ok ? "Matrícula anulada." : "No existe esa matrícula.");
                        }
                        case 10 -> mostrar(matriculaDao.alumnosDeModulo(
                                leerTexto("Código del módulo (MP0485...): ").toUpperCase(), leerCursoAcademico()));
                        case 11 -> {
                            for (Curso c : cursoDao.listarConModulos()) {
                                System.out.println("\n" + c + " - " + c.getHorasTotales() + " h");
                                c.getModulos().forEach(mod -> System.out.println("   " + mod));
                            }
                        }
                        case 12 -> {
                            String codigo = leerTexto("Código del módulo (MP0373...): ").toUpperCase();
                            Modulo mod = moduloDao.buscarPorCodigo(codigo);
                            if (mod == null) {
                                System.out.println("No existe ese módulo.");
                            } else {
                                System.out.println(mod + " se imparte en:");
                                moduloDao.cursosDondeSeImparte(codigo).forEach(c -> System.out.println("   " + c));
                            }
                        }
                        case 13 -> moduloDao.listarCompartidos().forEach(System.out::println);
                        case 0 -> System.out.println("¡Hasta luego!");
                        default -> System.out.println("Opción no válida.");
                    }
                } catch (IllegalArgumentException e) {   // errores de validación del DAO
                    System.out.println("No se ha podido completar: " + e.getMessage());
                }
            } while (opcion != 0);
        }
    }

    /** Pide los datos de una matrícula: alumno, curso académico, curso y módulos. */
    private static void nuevaMatricula(MatriculaDao matriculaDao, CursoDao cursoDao) {
        int alumnoId = leerEntero("Id del alumno: ");
        String cursoAcademico = leerCursoAcademico();
        String codigoCurso = leerTexto("Curso (DAM1, DAW2...): ").toUpperCase();

        Curso curso = cursoDao.buscarPorCodigo(codigoCurso);
        if (curso == null) {
            System.out.println("No existe ese curso.");
            return;
        }
        System.out.println("""
                Módulos: escribe los códigos separados por comas (p. ej. MP0486,MP0484).
                Puedes incluir módulos pendientes de otro curso del ciclo.
                Deja vacío para matricular el curso completo.""");
        String texto = leerTexto("Módulos: ").toUpperCase();
        List<String> codigos = texto.isEmpty() ? List.of()
                : Arrays.stream(texto.split(",")).map(String::trim).filter(c -> !c.isEmpty()).distinct().toList();

        Integer id = matriculaDao.matricular(alumnoId, cursoAcademico, codigoCurso, codigos);
        System.out.println("Matrícula creada con id " + id);
    }

    private static String leerCursoAcademico() {
        String texto = leerTexto("Curso académico (vacío = " + CURSO_ACTUAL + "): ");
        return texto.isEmpty() ? CURSO_ACTUAL : texto;
    }

    private static void mostrar(List<Alumno> alumnos) {
        if (alumnos.isEmpty()) {
            System.out.println("No hay alumnos.");
        } else {
            alumnos.forEach(System.out::println);
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número entero.");
            }
        }
    }
}
