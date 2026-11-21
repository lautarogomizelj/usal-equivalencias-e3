package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.repository.PermisoCarreraRepository;
import com.usal.e3.repository.PermisoSolicitudRepository;
import com.usal.e3.repository.UsuarioRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class PermisosServiceTest {

    private final PermisoCarreraRepository repositorio = mock(PermisoCarreraRepository.class);
    private final PermisoSolicitudRepository temporales = mock(PermisoSolicitudRepository.class);
    private final PermisosService permisos =
            new PermisosService(repositorio, temporales, mock(UsuarioRepository.class));

    @Test
    void academicoYSecretariaVenSusCarrerasActivas() {
        Carrera carrera = new Carrera();
        for (String rol : List.of(Rol.ACADEMICO, Rol.SECRETARIA)) {
            Cuenta cuenta = mock(Cuenta.class);
            when(cuenta.rolCodigo()).thenReturn(rol);
            when(cuenta.getId()).thenReturn(7);
            when(repositorio.carrerasActivas(7)).thenReturn(List.of(carrera));

            assertThat(permisos.carrerasConPermiso(cuenta)).containsExactly(carrera);
        }
    }

    @Test
    void elRestoDeLosRolesNoTrabajaPorCarrera() {
        assertThat(permisos.carrerasConPermiso(new Aspirante("a@b.com", "h", "A", "B", "1", "12345678"))).isNull();
        verifyNoInteractions(repositorio);
    }

    @Test
    void unPermisoTemporalVigenteAlcanzaParaParticipar() {
        when(temporales.activo(eq(7), eq(42), any())).thenReturn(true);

        assertThat(permisos.esParticipante(personal(Rol.ACADEMICO), solicitud())).isTrue();
    }

    @Test
    void unPermisoDeLaCarreraAlcanzaParaParticipar() {
        when(repositorio.activo(7, 1)).thenReturn(true);

        assertThat(permisos.esParticipante(personal(Rol.SECRETARIA), solicitud())).isTrue();
    }

    @Test
    void sinPermisosEsObservador() {
        assertThat(permisos.esParticipante(personal(Rol.ACADEMICO), solicitud())).isFalse();
    }

    @Test
    void adminYRevisorNoParticipan() {
        assertThat(permisos.esParticipante(personal(Rol.ADMIN), solicitud())).isFalse();
        assertThat(permisos.esParticipante(personal(Rol.REVISOR), solicitud())).isFalse();
        verifyNoInteractions(repositorio, temporales);
    }

    private static Cuenta personal(String rol) {
        Cuenta cuenta = mock(Cuenta.class);
        when(cuenta.rolCodigo()).thenReturn(rol);
        when(cuenta.getId()).thenReturn(7);
        return cuenta;
    }

    private static Solicitud solicitud() {
        Carrera carrera = mock(Carrera.class);
        when(carrera.getId()).thenReturn(1);
        Solicitud solicitud = mock(Solicitud.class);
        when(solicitud.getId()).thenReturn(42);
        when(solicitud.getCarreraDestino()).thenReturn(carrera);
        return solicitud;
    }
}
