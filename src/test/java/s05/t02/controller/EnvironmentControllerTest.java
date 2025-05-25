package s05.t02.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// Anotaciones de Spring Boot para test MVC
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
// Herramienta principal para simular requests HTTP
import org.springframework.test.web.servlet.MockMvc;
// Servicio mockeado
import s05.t02.config.TestSecurityConfig;
import s05.t02.model.Environment;
import s05.t02.model.User;
import s05.t02.model.dto.EnvironmentCreateRequest;
import s05.t02.model.dto.EnvironmentDTO;
import s05.t02.model.dto.EnvironmentUpdateRequest;
import s05.t02.model.enums.EnvironmentColor;
import s05.t02.model.enums.EnvironmentStatus;
import s05.t02.repository.EnvironmentRepository;
import s05.t02.repository.UserRepository;
import s05.t02.service.EnvironmentService;

import s05.t02.model.enums.UserRole;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Test de integración sobre EnvironmentController usando WebMvcTest.
 */
@Import(TestSecurityConfig.class)
@ExtendWith(SpringExtension.class) // Habilita Spring en el entorno de test
@WebMvcTest(EnvironmentController.class) // Solo se levanta el controller bajo test
class EnvironmentControllerTest {

    @Autowired
    private MockMvc mockMvc; // Cliente HTTP simulado para probar endpoints

    @Autowired
    private ObjectMapper objectMapper; // Serializa y deserializa JSON en los tests

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private EnvironmentRepository environmentRepository;
    @MockBean
    private EnvironmentService environmentService; // Mock del servicio que usa el controller

    @Test
    @WithMockUser(username = "fer", roles = {"USER"})
    @DisplayName("GET /environments devuelve entornos del usuario autenticado")
    void shouldReturnUserEnvironments() throws Exception {
        // Simulamos usuario
        User mockUser = User.builder()
                .id(10L)
                .username("fer")
                .password("encrypted") // no importa el valor real
                .role(UserRole.ROLE_USER)
                .build();

        // Simulamos entornos
        List<Environment> mockEnvs = List.of(
                Environment.builder()
                        .id(1L)
                        .title("CV Diseño")
                        .description("Mi entorno de diseño")
                        .color(EnvironmentColor.BLUE)
                        .url(null)
                        .status(EnvironmentStatus.IDLE)
                        .user(mockUser)
                        .build(),

                Environment.builder()
                        .id(2L)
                        .title("CV Desarrollo")
                        .description("Entorno con archivos")
                        .color(EnvironmentColor.GREEN)
                        .url("https://cloudinary.com/archivo.pdf")
                        .status(EnvironmentStatus.ACTIVE)
                        .user(mockUser)
                        .build()
        );

        // Mock del servicio
        Mockito.when(environmentService.getUserEnvironments("fer"))
                .thenReturn(mockEnvs);

        // Ejecutamos y verificamos
        mockMvc.perform(get("/environments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("CV Diseño"))
                .andExpect(jsonPath("$[1].url").value("https://cloudinary.com/archivo.pdf"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("GET /environments/{id} devuelve el entorno por ID si el usuario es dueño")
    void shouldReturnEnvironmentById() throws Exception {
        // Creamos un mock de usuario
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encrypted")
                .role(UserRole.ROLE_USER)
                .build();

        // Creamos un entorno asociado a ese usuario
        Environment environment = Environment.builder()
                .id(10L)
                .title("CV Designer")
                .description("Diseño UX")
                .color(EnvironmentColor.YELLOW)
                .url("http://cloud.com/file.png")
                .status(EnvironmentStatus.ACTIVE)
                .user(user)
                .build();

        // Mock del método del servicio que usa el controller
        given(environmentService.getEnvironmentById(10L, "testuser"))
                .willReturn(environment);

        // Ejecutamos el GET y verificamos el contenido del JSON
        mockMvc.perform(get("/environments/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("CV Designer"))
                .andExpect(jsonPath("$.description").value("Diseño UX"))
                .andExpect(jsonPath("$.color").value("YELLOW"))
                .andExpect(jsonPath("$.url").value("http://cloud.com/file.png"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @WithMockUser(username = "fer", roles = {"USER"})
    @DisplayName("POST /environments crea un nuevo entorno")
    void shouldCreateEnvironment() throws Exception {
        // Cuerpo del request (input)
        EnvironmentCreateRequest request = new EnvironmentCreateRequest(
                "CV Diseño",
                "Mi entorno de diseño",
                EnvironmentColor.BLUE,
                null
        );

        // Simulamos el DTO de respuesta (output esperado)
        EnvironmentDTO responseDTO = new EnvironmentDTO(
                1L,
                "CV Diseño",
                "Mi entorno de diseño",
                EnvironmentColor.BLUE,
                null,
                EnvironmentStatus.IDLE,
                10L
        );

        // Mock del servicio
        Mockito.when(environmentService.createEnvironment(Mockito.any(), Mockito.eq("fer")))
                .thenReturn(responseDTO);

        // Ejecutamos el request y verificamos
        mockMvc.perform(post("/environments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("CV Diseño"))
                .andExpect(jsonPath("$.status").value("IDLE"));
    }

    @Test
    @WithMockUser(username = "fer", roles = {"USER"})
    @DisplayName("PUT /environments/{id} actualiza el entorno existente del usuario")
    void shouldUpdateEnvironment() throws Exception {
        // Request que envía el usuario
        EnvironmentUpdateRequest updateRequest = new EnvironmentUpdateRequest(
                "Nuevo título",
                "Descripción modificada",
                EnvironmentColor.RED
        );

        // Mock de la entidad que retorna el servicio
        Environment mockEnvironment = Environment.builder()
                .id(1L)
                .title("Nuevo título")
                .description("Descripción modificada")
                .color(EnvironmentColor.RED)
                .url("https://archivo-actualizado.com")
                .status(EnvironmentStatus.ACTIVE)
                .user(User.builder().id(10L).build())
                .build();

        // Mockeo del servicio (3 argumentos)
        Mockito.when(environmentService.updateEnvironment(Mockito.eq(1L), Mockito.any(EnvironmentUpdateRequest.class), Mockito.eq("fer")))
                .thenReturn(mockEnvironment);

        // Ejecutar PUT y verificar
        mockMvc.perform(put("/environments/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Nuevo título"))
                .andExpect(jsonPath("$.description").value("Descripción modificada"))
                .andExpect(jsonPath("$.color").value("RED"))
                .andExpect(jsonPath("$.url").value("https://archivo-actualizado.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userId").value(10L));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("DELETE /environments/{id} elimina el entorno si pertenece al usuario")
    void shouldDeleteEnvironmentSuccessfully() throws Exception {
        Long environmentId = 1L;

        // No se necesita simular un retorno, solo verificar que el método se llama
        doNothing().when(environmentService).deleteEnvironment(environmentId, "testuser");

        mockMvc.perform(delete("/environments/{id}", environmentId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(environmentService, times(1)).deleteEnvironment(environmentId, "testuser");
    }

}