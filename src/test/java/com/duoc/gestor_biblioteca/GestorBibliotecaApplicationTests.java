package com.duoc.gestor_biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Levanta el contexto completo contra H2 (perfil test): no necesita Oracle ni el .env.
@SpringBootTest
@ActiveProfiles("test")
class GestorBibliotecaApplicationTests {

	@Test
	void contextLoads() {
	}

}
