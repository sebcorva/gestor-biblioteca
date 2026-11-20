package com.duoc.gestor_biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.duoc.gestor_biblioteca.config.OracleJdbcUrl;
import com.duoc.gestor_biblioteca.config.OracleWalletBootstrap;

@SpringBootApplication
public class GestorBibliotecaApplication {

	public static void main(String[] args) {
		// En AWS: baja la wallet de S3 a /app/wallet (no hace nada si no hay bucket configurado)
		OracleWalletBootstrap.loadFromEnvironment();
		// DB_URL puede venir como alias TNS (Secrets Manager) y se completa con TNS_ADMIN
		String databaseUrl = System.getenv("DB_URL");
		if (databaseUrl != null && !databaseUrl.isBlank()) {
			System.setProperty("DB_URL", OracleJdbcUrl.normalize(databaseUrl, System.getenv("TNS_ADMIN")));
		}
		SpringApplication.run(GestorBibliotecaApplication.class, args);
	}

}
