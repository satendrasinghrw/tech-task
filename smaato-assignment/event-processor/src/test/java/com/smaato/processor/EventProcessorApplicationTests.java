package com.smaato.processor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventProcessorApplicationTests {
	@LocalServerPort
	private int port;

	//@Test
	void contextLoads() throws URISyntaxException {
		RestTemplate restTemplate = new RestTemplate();
		final String baseUrl = "http://localhost:" + port + "/query/receive";

		URI uri = new URI(baseUrl);
		HttpEntity<Long> request = new HttpEntity<>(123L, new HttpHeaders());
		ResponseEntity<HttpStatus> result = restTemplate.postForEntity(uri, request, HttpStatus.class);
		Assert.isTrue(HttpStatus.OK.value() == result.getStatusCodeValue(), "OK");
	}
}
