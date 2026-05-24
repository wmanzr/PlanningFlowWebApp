package RUT.PlanningFlow.integration;

import RUT.PlanningFlow.application.port.out.AIPort;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

 
@TestConfiguration
public class IntegrationTestAiStubConfiguration {

    @Bean
    @Primary
    public AIPort integrationTestAiPort() {
        final AIPort stub = Mockito.mock(AIPort.class);
        when(stub.complete(anyString(), anyString())).thenReturn("{\"integrationTest\":true}");
        return stub;
    }
}
