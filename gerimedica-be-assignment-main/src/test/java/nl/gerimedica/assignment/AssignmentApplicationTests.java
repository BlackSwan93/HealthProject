package nl.gerimedica.assignment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AssignmentApplicationTests {
	@Value("${local.server.port}")
	private int port;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		restTemplate = new RestTemplate();
	}

	@Test
	void testSuccess() {
		String url = "http://localhost:" + port + "/api/appointments-by-reason?keyword=Checkup";
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
	}
}