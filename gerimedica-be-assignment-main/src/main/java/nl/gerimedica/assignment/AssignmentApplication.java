package nl.gerimedica.assignment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

// This is the main entry point of the Appointment Management System.
// It starts up the Spring Boot application and extends SpringBootServletInitializer to allow deployment to external servlet containers if needed.

@SpringBootApplication
public class AssignmentApplication {

	private static final Logger logger = LoggerFactory.getLogger(AssignmentApplication.class);

	public static void main(String[] args) {

		try {
			SpringApplication app = new SpringApplication(AssignmentApplication.class);
			Environment env = app.run(args).getEnvironment();

			logger.info("""
							Application '{}' is running! Access URLs:
							Local: \thttp://localhost:{}
							""",
					env.getProperty("spring.application.name"),
					env.getProperty("server.port")
			);
		} catch (Exception e) {
			logger.error("Error starting application", e);
			throw e;
		}

	}
}
