package com.example.instituto.datos;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.instituto.model.Alumno;
import com.example.instituto.model.Ciclo;
import com.example.instituto.model.Curso;
import com.example.instituto.model.Matricula;
import com.example.instituto.model.Modulo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Precarga los ciclos, cursos y módulos de ASIR, DAM y DAW si la BD está vacía,
 * y unos alumnos de ejemplo con sus matrículas.
 *
 * Datos tomados de la web de FP de la Xunta de Galicia (curso 2023-2024):
 *   https://www.edu.xunta.gal/fp/ciclo/D3IFC000100  (ASIR)
 *   https://www.edu.xunta.gal/fp/ciclo/D3IFC000200  (DAM)
 *   https://www.edu.xunta.gal/fp/ciclo/D3IFC000300  (DAW)
 *
 * Se usa solo la API de JPA: se crean objetos Java, se enlazan entre sí y se
 * guardan con persist(). JPA genera todos los INSERT, incluidos los de la
 * tabla intermedia Curso_Modulo.
 */
public final class DatosIniciales {

    private static final String FAMILIA = "Informática e comunicacións";

    private DatosIniciales() {
    }

    public static void cargarSiVacia(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            long ciclos = em.createQuery("SELECT COUNT(c) FROM Ciclo c", Long.class).getSingleResult();
            if (ciclos > 0) {
                return; // ya cargados
            }

            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                cargar(em);
                tx.commit();
                System.out.println("Datos iniciales cargados: ciclos, cursos, módulos, alumnos y matrículas.");
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    private static void cargar(EntityManager em) {
        // ---------- Módulos (cada uno una sola vez, aunque se imparta en varios cursos) ----------
        Map<String, Modulo> m = new HashMap<>();
        for (Modulo modulo : List.of(
                // Comunes a 1º de los tres ciclos
                new Modulo("MP0179", "Inglés profesional (GS)", 60, 2),
                new Modulo("MP1709", "Itinerario persoal para a empregabilidade I", 107, 4),
                new Modulo("MP0373", "Linguaxes de marcas e sistemas de xestión de información", 103, 4),
                new Modulo("MP1708", "Sostibilidade aplicada ao sistema produtivo", 30, 1),
                // Comunes a 1º de DAM y DAW
                new Modulo("MP0483", "Sistemas informáticos", 155, 6),
                new Modulo("MP0484", "Bases de datos", 187, 7),
                new Modulo("MP0485", "Programación", 240, 9),
                new Modulo("MP0487", "Contornos de desenvolvemento", 78, 3),
                // 1º ASIR
                new Modulo("MP0369", "Implantación de sistemas operativos", 213, 8),
                new Modulo("MP0370", "Planificación e administración de redes", 213, 8),
                new Modulo("MP0371", "Fundamentos de hardware", 78, 3),
                new Modulo("MP0372", "Xestión de bases de datos", 156, 6),
                // Comunes a 2º de los tres ciclos
                new Modulo("MO0002", "Habilidades comunicativas en lingua estranxeira (GS)", 60, 2),
                new Modulo("MO0004", "Afondamento nas competencias profesionais (GS)", 30, 1),
                new Modulo("MP1665", "Dixitalización aplicada aos sectores produtivos (GS)", 30, 1),
                new Modulo("MP1710", "Itinerario persoal para a empregabilidade II", 50, 2),
                // 2º ASIR
                new Modulo("MP0374", "Administración de sistemas operativos", 219, 8),
                new Modulo("MP0375", "Servizos de rede e internet", 191, 7),
                new Modulo("MP0376", "Implantación de aplicacións web", 164, 6),
                new Modulo("MP0377", "Administración de sistemas xestores de bases de datos", 109, 4),
                new Modulo("MP0378", "Seguridade e alta dispoñibilidade", 137, 5),
                new Modulo("MP0379", "Proxecto intermodular de administración de sistemas informáticos en rede", 50, 0),
                // 2º DAM
                new Modulo("MP0486", "Acceso a datos", 220, 8),
                new Modulo("MP0488", "Desenvolvemento de interfaces", 191, 7),
                new Modulo("MP0489", "Programación multimedia e dispositivos móbiles", 191, 7),
                new Modulo("MP0490", "Programación de servizos e procesos", 109, 4),
                new Modulo("MP0491", "Sistemas de xestión empresarial", 109, 4),
                new Modulo("MP0492", "Proxecto intermodular de desenvolvemento de aplicacións multiplataforma", 50, 0),
                // 2º DAW
                new Modulo("MP0612", "Desenvolvemento web en contorno cliente", 219, 8),
                new Modulo("MP0613", "Desenvolvemento web en contorno servidor", 273, 10),
                new Modulo("MP0614", "Despregamento de aplicacións web", 109, 4),
                new Modulo("MP0615", "Deseño de interfaces web", 219, 8),
                new Modulo("MP0616", "Proxecto intermodular de desenvolvemento de aplicacións web", 50, 0))) {
            em.persist(modulo);
            m.put(modulo.getCodigo(), modulo);
        }

        // ---------- Ciclos ----------
        Ciclo asir = new Ciclo("D3IFC000100", "ASIR", "Administración de sistemas informáticos en rede", FAMILIA, "superior", 2000);
        Ciclo dam = new Ciclo("D3IFC000200", "DAM", "Desenvolvemento de aplicacións multiplataforma", FAMILIA, "superior", 2000);
        Ciclo daw = new Ciclo("D3IFC000300", "DAW", "Desenvolvemento de aplicacións web", FAMILIA, "superior", 2000);
        em.persist(asir);
        em.persist(dam);
        em.persist(daw);

        // ---------- Cursos y sus módulos ----------
        List<String> comunes1 = List.of("MP0179", "MP1709", "MP0373", "MP1708");
        List<String> comunes2 = List.of("MO0002", "MO0004", "MP1665", "MP1710");
        List<String> programacion1 = List.of("MP0483", "MP0484", "MP0485", "MP0487");

        crearCurso(em, m, "ASIR1", 1, asir, comunes1, List.of("MP0369", "MP0370", "MP0371", "MP0372"));
        Curso asir2 = crearCurso(em, m, "ASIR2", 2, asir, comunes2, List.of("MP0374", "MP0375", "MP0376", "MP0377", "MP0378", "MP0379"));
        Curso dam1 = crearCurso(em, m, "DAM1", 1, dam, comunes1, programacion1);
        Curso dam2 = crearCurso(em, m, "DAM2", 2, dam, comunes2, List.of("MP0486", "MP0488", "MP0489", "MP0490", "MP0491", "MP0492"));
        Curso daw1 = crearCurso(em, m, "DAW1", 1, daw, comunes1, programacion1);
        crearCurso(em, m, "DAW2", 2, daw, comunes2, List.of("MP0612", "MP0613", "MP0614", "MP0615", "MP0616"));

        // ---------- Alumnos de ejemplo y sus matrículas ----------
        Alumno uxia = new Alumno("Uxía", "Castro Varela");
        Alumno brais = new Alumno("Brais", "Otero Pazos");
        Alumno antia = new Alumno("Antía", "Rey Souto");
        em.persist(uxia);
        em.persist(brais);
        em.persist(antia);

        // Uxía: 1º de DAM completo el año pasado...
        Matricula m1 = new Matricula(uxia, "2025-2026", dam1, LocalDate.of(2025, 9, 10));
        m1.getModulos().addAll(dam1.getModulos());
        // ...y este año 2º de DAM completo + "Bases de datos" pendiente de 1º
        Matricula m2 = new Matricula(uxia, "2026-2027", dam2, LocalDate.of(2026, 9, 9));
        m2.getModulos().addAll(dam2.getModulos());
        m2.getModulos().add(m.get("MP0484"));

        // Brais: matrícula parcial, solo algunos módulos de 1º de DAW
        Matricula m3 = new Matricula(brais, "2026-2027", daw1, LocalDate.of(2026, 9, 11));
        m3.getModulos().addAll(List.of(m.get("MP0485"), m.get("MP0484"), m.get("MP0373")));

        // Antía: 2º de ASIR completo
        Matricula m4 = new Matricula(antia, "2026-2027", asir2, LocalDate.of(2026, 9, 9));
        m4.getModulos().addAll(asir2.getModulos());

        for (Matricula matricula : List.of(m1, m2, m3, m4)) {
            em.persist(matricula);
        }
    }

    private static Curso crearCurso(EntityManager em, Map<String, Modulo> modulos,
                                   String codigo, int numero, Ciclo ciclo,
                                   List<String> comunes, List<String> especificos) {
        Curso curso = new Curso(codigo, numero, ciclo);
        comunes.forEach(c -> curso.getModulos().add(modulos.get(c)));
        especificos.forEach(c -> curso.getModulos().add(modulos.get(c)));
        em.persist(curso);
        return curso;
    }
}
