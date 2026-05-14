package RUT.PlanningFlow;

import RUT.PlanningFlow.config.ai.GigaChatProperties;
import RUT.PlanningFlow.config.external.ExternalSupplySimulationProperties;
import RUT.PlanningFlow.adapter.in.web.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, GigaChatProperties.class, ExternalSupplySimulationProperties.class})
public class PlanningFlowApplication {

	public static void main(String[] args) {
		loadEnvFile();
		SpringApplication.run(PlanningFlowApplication.class, args);
	}

	private static void loadEnvFile() {
		try {
			File envFile = new File(".env");
			if (!envFile.exists()) {
				envFile = Paths.get(System.getProperty("user.dir"), ".env").toFile();
			}
			if (!envFile.exists()) {
				envFile = Paths.get(System.getProperty("user.dir"), "PlanningFlow", ".env").toFile();
			}
			if (!envFile.exists()) {
				return;
			}
			final Properties props = new Properties();
			try (FileInputStream fis = new FileInputStream(envFile)) {
				props.load(fis);
			}
			props.forEach((key, value) -> {
				final String keyStr = key.toString();
				String valueStr = value.toString().trim();
				if (valueStr.length() >= 2 && valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
					valueStr = valueStr.substring(1, valueStr.length() - 1);
				}
				if (System.getenv(keyStr) == null && System.getProperty(keyStr) == null) {
					System.setProperty(keyStr, valueStr);
					if ("JWT_SECRET".equals(keyStr)) {
						System.setProperty("jwt.secret", valueStr);
					}
				}
			});
		} catch (final Exception ignored) {
		}
	}
}
