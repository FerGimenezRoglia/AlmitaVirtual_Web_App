package s05.t02.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import s05.t02.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración para el controlador de autenticación (/auth/register y /auth/login).
 */
@Transactional // Asegura que cada test se ejecute con rollback (no modifica la BD realmente)
public class AuthControllerTest extends BaseIntegrationTest {

    @Test
    void testUserCanRegisterAndLogin() throws Exception {
        var username = "testuser";
        var password = "Test1234!";

        // register
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser",
                                  "password": "Test1234!",
                                  "recoveryKey": "TestKey123!"
                                }
                                """))
                .andExpect(status().isOk()); // Esperamos un HTTP 200

        // login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "testuser",
                                  "password": "Test1234!"
                                }
                                """))
                .andExpect(status().isOk()) // Login exitoso
                .andExpect(jsonPath("$.token").exists()); // Verificamos que haya un token en la respuesta
    }
}