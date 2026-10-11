package fft_battleground.config;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import lombok.extern.slf4j.Slf4j;

/**
 * Starts a managed PostgreSQL instance for local development, so no system-wide
 * install is needed. Only activates when explicitly requested at launch:
 *
 *   java -Dfft_battleground.embeddedPostgres=true -jar target/Viewer.war
 *
 * or with the environment variable FFTB_EMBEDDED_POSTGRES=true. It is
 * deliberately NOT readable from application.properties so a production
 * deployment can never turn it on by accident.
 *
 * Data lives in ./pgdata and survives restarts; delete that folder to reset.
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EmbeddedPostgresInitializer implements EnvironmentPostProcessor {

	private static final String ENABLE_FLAG = "fft_battleground.embeddedPostgres";
	private static final String DATABASE_NAME = "FFT_BattleGround";
	private static final int PORT = 5432;

	private EmbeddedPostgres postgres;

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String flag = environment.getProperty(ENABLE_FLAG);
		if (flag == null) {
			flag = System.getenv("FFTB_EMBEDDED_POSTGRES");
		}
		if (!"true".equalsIgnoreCase(flag)) {
			return;
		}

		try {
			log.info("Starting embedded PostgreSQL on port {}...", PORT);
			this.postgres = EmbeddedPostgres.builder()
					.setPort(PORT)
					.setDataDirectory(Paths.get("pgdata"))
					.setCleanDataDirectory(false)
					.start();
			this.createDatabaseIfMissing();
			log.info("Embedded PostgreSQL ready on port {}", PORT);

			Map<String, Object> props = new HashMap<>();
			props.put("spring.datasource.url", "jdbc:postgresql://localhost:" + PORT + "/" + DATABASE_NAME);
			props.put("spring.datasource.username", "postgres");
			props.put("spring.datasource.password", "postgres");
			environment.getPropertySources().addFirst(new MapPropertySource("embeddedPostgres", props));

			Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "embedded-postgres-shutdown"));
		} catch (Exception e) {
			throw new IllegalStateException("Failed to start embedded PostgreSQL", e);
		}
	}

	private void createDatabaseIfMissing() throws Exception {
		try (Connection conn = this.postgres.getPostgresDatabase().getConnection();
				Statement stmt = conn.createStatement()) {
			conn.setAutoCommit(true);
			try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_roles WHERE rolname = 'postgres'")) {
				if (!rs.next()) {
					stmt.execute("CREATE ROLE postgres LOGIN SUPERUSER");
				}
			}
			try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + DATABASE_NAME + "'")) {
				if (!rs.next()) {
					log.info("Creating database {}", DATABASE_NAME);
					stmt.execute("CREATE DATABASE \"" + DATABASE_NAME + "\"");
				}
			}
		}
	}

	private void stop() {
		try {
			if (this.postgres != null) {
				this.postgres.close();
			}
		} catch (Exception e) {
			log.warn("Error stopping embedded PostgreSQL", e);
		}
	}
}
