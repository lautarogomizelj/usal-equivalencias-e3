package com.usal.e3.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PruebaController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void errorDeNegocioUsaElFormatoDelContrato() throws Exception {
        mvc.perform(get("/transicion"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("TRANSICION_INVALIDA"))
                .andExpect(jsonPath("$.message").value(ErrorCode.TRANSICION_INVALIDA.mensaje()))
                .andExpect(jsonPath("$.path").value("/transicion"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void validacionAgrupaLosErroresPorCampo() throws Exception {
        mvc.perform(post("/validar").contentType(MediaType.APPLICATION_JSON).content("{\"comentario\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDACION"))
                .andExpect(jsonPath("$.errors.comentario[0]").value("Es obligatorio"));
    }

    @Test
    void errorInesperadoNoExponeElDetalle() throws Exception {
        mvc.perform(get("/roto"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.message").value(ErrorCode.ERROR_INTERNO.mensaje()));
    }

    record Cuerpo(@NotBlank(message = "Es obligatorio") String comentario) {}

    @RestController
    static class PruebaController {

        @GetMapping("/transicion")
        void transicion() {
            throw new ApiException(ErrorCode.TRANSICION_INVALIDA);
        }

        @PostMapping("/validar")
        void validar(@Valid @RequestBody Cuerpo cuerpo) {}

        @GetMapping("/roto")
        void roto() {
            throw new IllegalStateException("detalle interno");
        }
    }
}
