package com.lisory.backend.pagamentos.asaas.config;

import com.lisory.backend.pagamentos.asaas.client.AsaasClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AsaasStartupIntegrationTest {

    @Autowired
    private AsaasClient asaasClient;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldStartSpringBootAndCreateAsaasClientBean() {
        assertNotNull(asaasClient);

        ResponseEntity<String> healthResponse = restTemplate.getForEntity("/actuator/health", String.class);
        assertTrue(healthResponse.getStatusCode().is2xxSuccessful());
    }
}
