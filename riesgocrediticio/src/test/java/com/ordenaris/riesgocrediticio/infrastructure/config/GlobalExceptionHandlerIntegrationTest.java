package com.ordenaris.riesgocrediticio.infrastructure.config;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void when404Exception_thenReturnNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/riesgo/evaluar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"empresaId\":\"NOEXISTE\",\"montoSolicitado\":100000,\"productoFinanciero\":\"LINEA_OPERATIVA\",\"fechaSolicitud\":\"2026-03-24\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenValidationError_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/riesgo/evaluar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"empresaId\":\"\",\"montoSolicitado\":100000,\"productoFinanciero\":\"LINEA_OPERATIVA\",\"fechaSolicitud\":\"2026-03-24\"}"))
                .andExpect(status().isBadRequest());
    }
}

