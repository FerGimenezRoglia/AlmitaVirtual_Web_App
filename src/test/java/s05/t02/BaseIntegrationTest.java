package s05.t02;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Clase base para todos los tests de integración.
 * - Usa @SpringBootTest para cargar todo el contexto de Spring.
 * - Usa @AutoConfigureMockMvc para poder simular llamadas HTTP a los controladores.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    // Inyectamos MockMvc para simular peticiones HTTP
    @Autowired
    protected MockMvc mockMvc;

    // Inyectamos ObjectMapper para convertir objetos a JSON y viceversa
    @Autowired
    protected ObjectMapper objectMapper;
}