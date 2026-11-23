package com.example.instituto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.instituto.dao.MatriculaDao;
import com.example.instituto.datos.DatosIniciales;
import com.example.instituto.model.Alumno;
import com.example.instituto.repository.AlumnoRepository;

import jakarta.persistence.EntityManagerFactory;

/**
 * Aplicación de consola con las operaciones CRUD básicas sobre Alumno
 * usando Spring Data JPA Repository.
 */
    @SpringBootApplication
    public class InstitutoApplication implements CommandLineRunner {
        private final AlumnoRepository alumnoRepository;
        private final EntityManagerFactory emf;

        // Spring inyecta los repositorios y la EntityManagerFactory que ha configurado
        public InstitutoApplication(AlumnoRepository alumnoRepository, EntityManagerFactory emf) {
            this.alumnoRepository = alumnoRepository;
            this.emf = emf;
        }

    public static void main(String[] args) {
        SpringApplication.run(InstitutoApplication.class, args);
    }

    private static final Scanner sc = new Scanner(System.in);

    @Override
    public void run(String... args) throws Exception {
        // Spring Boot ya configura automáticamente el EntityManagerFactory y las transacciones,
        // por lo que puedes usar directamente alumnoRepository.
        DatosIniciales.cargarSiVacia(emf);   // misma precarga que en Main (JPA estándar)

        int opcion;
        do {
            System.out.println("""

                    ===== GESTIÓN DE ALUMNOS =====
                    1. Listar alumnos
                    2. Listar alumnos de un curso
                    3. Buscar alumno por id
                    4. Nuevo alumno
                    5. Modificar alumno
                    6. Borrar alumno
                    7. Número de alumnos matriculados por curso
                    0. Salir""");
            opcion = leerEntero("Opción: ");

            switch (opcion) {
                case 1 -> mostrar(alumnoRepository.findAllOrdenados());
                case 2 -> mostrar(alumnoRepository.findMatriculados(leerTexto("Curso: ").toUpperCase(),
                        MatriculaDao.cursoAcademicoDe(LocalDate.now())));
                case 3 -> {
                    Alumno a = alumnoRepository.findById(leerEntero("Id: ")).orElse(null);
                    System.out.println(a != null ? a : "No existe ningún alumno con ese id.");
                }
                case 4 -> {
                    Alumno nuevo = new Alumno(leerTexto("Nombre: "), leerTexto("Apellidos: "));
                    // Corrección: save devuelve la entidad guardada, imprimir su id
                    Alumno guardado = alumnoRepository.save(nuevo);
                    System.out.println("Alumno guardado con id " + guardado.getId());
                }
                case 5 -> {
                    int id = leerEntero("Id del alumno a modificar: ");
                    // Corrección: primero buscar, luego modificar, finalmente save
                    Optional<Alumno> opt = alumnoRepository.findById(id);
                    if (opt.isPresent()) {
                        Alumno alumno = opt.get();
                        alumno.setNombre(leerTexto("Nuevo nombre: "));
                        alumno.setApellidos(leerTexto("Nuevos apellidos: "));
                        alumnoRepository.save(alumno);
                        System.out.println("Alumno modificado.");
                    } else {
                        System.out.println("No existe ningún alumno con ese id.");
                    }
                }
                case 6 -> {
                    int id = leerEntero("Id del alumno a borrar: ");
                    // Corrección: deleteById no devuelve boolean, primero verificar si existe
                    if (alumnoRepository.existsById(id)) {
                        alumnoRepository.deleteById(id);
                        System.out.println("Alumno borrado.");
                    } else {
                        System.out.println("No existe ningún alumno con ese id.");
                    }
                }
                case 7 -> mostrarRecuento(leerCursoAcademico());
                case 0 -> System.out.println("¡Hasta luego!");
                default -> System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    /**
     * Muestra cuántos alumnos hay matriculados en cada curso.
     * La consulta devuelve dos valores por fila (código y número), no una entidad,
     * así que cada fila llega como un Object[] y hay que hacer un cast de cada columna.
     */
    private void mostrarRecuento(String cursoAcademico) {
        List<Object[]> filas = alumnoRepository.contarPorCurso(cursoAcademico);
        if (filas.isEmpty()) {
            System.out.println("No hay matrículas en " + cursoAcademico);
            return;
        }
        System.out.println("Alumnos matriculados en " + cursoAcademico + ":");
        for (Object[] fila : filas) {
            String curso = (String) fila[0];   // m.curso.codigo
            Long total = (Long) fila[1];       // COUNT(m) siempre devuelve Long
            System.out.printf("   %-6s %3d%n", curso, total);
        }
    }

    private static String leerCursoAcademico() {
        String actual = MatriculaDao.cursoAcademicoDe(LocalDate.now());
        String texto = leerTexto("Curso académico (vacío = " + actual + "): ");
        return texto.isEmpty() ? actual : texto;
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