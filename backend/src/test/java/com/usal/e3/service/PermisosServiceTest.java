package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Rol;
import com.usal.e3.repository.PermisoCarreraRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

class PermisosServiceTest {

    private final PermisoCarreraRepository repositorio = mock(PermisoCarreraRepository.class);
    private final PermisosService permisos = new PermisosService(repositorio);

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
        assertThat(permisos.carrerasConPermiso(new Aspirante("a@b.com", "h", "A", "1", "12345678"))).isNull();
        verifyNoInteractions(repositorio);
    }
}
