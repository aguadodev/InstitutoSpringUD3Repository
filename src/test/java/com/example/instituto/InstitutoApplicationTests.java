package com.example.instituto;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InstitutoApplicationTests {

	// Al arrancar el contexto se ejecuta el menú de consola (CommandLineRunner):
	// le pasamos la opción "0" (Salir) para que no se quede esperando al teclado.
	static {
		System.setIn(new ByteArrayInputStream("0\n".getBytes()));
	}

	@Test
	void contextLoads() {
	}

}
