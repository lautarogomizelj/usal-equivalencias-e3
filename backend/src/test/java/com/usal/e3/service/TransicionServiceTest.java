package com.usal.e3.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.DatosOrigen;
import com.usal.e3.domain.Estado;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Solicitud;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.HistorialEstadoRepository;
import java.time.OffsetDateTime;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

class TransicionServiceTest {

    private static final String IP = "10.0.0.1";

    private final HistorialEstadoRepository historial = mock(HistorialEstadoRepository.class);
    private final PermisosService permisos = mock(PermisosService.class);
    private final AuditoriaService auditoria = mock(AuditoriaService.class);
    private final NotificacionService notificaciones = mock(NotificacionService.class);
    private final TransicionService motor = new TransicionService(historial, permisos, auditoria, notificaciones);

    private final Cuenta duenio = aspirante(15);
    private final Solicitud solicitud = solicitudDe(15);

    @Test
    void elDuenioEnviaLaDocumentacionYQuedaRegistrado() {
        Transicion transicion =
                motor.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, Estado.SOL_INI, duenio);
        motor.aplicar(solicitud, transicion, duenio, IP);

        assertThat(solicitud.getEstadoCodigo()).isEqualTo(Estado.DOC_REV);
        verify(historial).save(argThat(cambio -> Estado.SOL_INI.equals(cambio.getEstadoAnteriorCodigo())
                && Estado.DOC_REV.equals(cambio.getEstadoCodigo())
                && cambio.getUsuarioUltimoCambio() == null));
        verify(auditoria).registrarCambioEstado(duenio, null, Estado.SOL_INI, Estado.DOC_REV, null, IP);
        verify(notificaciones).cambioDeEstado(solicitud, Estado.SOL_INI, null);
    }

    @Test
    void elPersonalSoloActuaSiEsParticipanteYResponsable() {
        Usuario academico = mock(Usuario.class);
        when(academico.tipo()).thenReturn(TitularTipo.USUARIO);
        when(academico.rolCodigo()).thenReturn(Rol.ACADEMICO);
        when(academico.getId()).thenReturn(7);
        when(permisos.esParticipante(academico, solicitud)).thenReturn(true);
        solicitud.cambiarEstado(Estado.DOC_REV, OffsetDateTime.now());

        assertThat(motor.accionesDisponibles(solicitud, academico)).isEmpty();
        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ACEPTAR_DOCUMENTACION, Estado.DOC_REV, academico),
                ErrorCode.NO_RESPONSABLE);

        Usuario otro = mock(Usuario.class);
        when(otro.getId()).thenReturn(8);
        solicitud.asignarResponsable(Rol.ACADEMICO, otro, OffsetDateTime.now());
        assertThat(motor.accionesDisponibles(solicitud, academico)).isEmpty();
        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ACEPTAR_DOCUMENTACION, Estado.DOC_REV, academico),
                ErrorCode.NO_RESPONSABLE);

        solicitud.asignarResponsable(Rol.ACADEMICO, academico, OffsetDateTime.now());
        assertThat(motor.accionesDisponibles(solicitud, academico))
                .containsExactly(TransicionService.RECHAZAR_DOCUMENTACION, TransicionService.ACEPTAR_DOCUMENTACION);

        Transicion rechazo =
                motor.validar(solicitud, TransicionService.RECHAZAR_DOCUMENTACION, Estado.DOC_REV, academico);
        motor.aplicar(solicitud, rechazo, academico, "Falta la firma", IP);

        assertThat(solicitud.getEstadoCodigo()).isEqualTo(Estado.DOC_RECH);
        verify(historial).save(argThat(cambio -> "Falta la firma".equals(cambio.getComentarios())
                && cambio.getUsuarioUltimoCambio() == academico));
        verify(notificaciones).cambioDeEstado(solicitud, Estado.DOC_REV, "Falta la firma");
    }

    @Test
    void elResponsableSinPermisoYaNoParticipa() {
        Usuario academico = mock(Usuario.class);
        when(academico.tipo()).thenReturn(TitularTipo.USUARIO);
        when(academico.rolCodigo()).thenReturn(Rol.ACADEMICO);
        when(academico.getId()).thenReturn(7);
        solicitud.asignarResponsable(Rol.ACADEMICO, academico, OffsetDateTime.now());
        solicitud.cambiarEstado(Estado.DOC_REV, OffsetDateTime.now());

        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ACEPTAR_DOCUMENTACION, Estado.DOC_REV, academico),
                ErrorCode.NO_PARTICIPANTE);
    }

    @Test
    void elAdminEjecutaLasDelPersonalSinPermisoNiResponsabilidadPeroNoLasDelAspirante() {
        Usuario admin = mock(Usuario.class);
        when(admin.tipo()).thenReturn(TitularTipo.USUARIO);
        when(admin.rolCodigo()).thenReturn(Rol.ADMIN);

        assertThat(motor.accionesDisponibles(solicitud, admin)).isEmpty();
        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, Estado.SOL_INI, admin),
                ErrorCode.ACCESO_DENEGADO);

        solicitud.cambiarEstado(Estado.DOC_REV, OffsetDateTime.now());
        assertThat(motor.accionesDisponibles(solicitud, admin))
                .containsExactly(TransicionService.RECHAZAR_DOCUMENTACION, TransicionService.ACEPTAR_DOCUMENTACION);
        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ACEPTAR_DOCUMENTACION, Estado.SOL_INI, admin),
                ErrorCode.TRANSICION_INVALIDA);

        Transicion aceptar = motor.validar(solicitud, TransicionService.ACEPTAR_DOCUMENTACION, Estado.DOC_REV, admin);
        motor.aplicar(solicitud, aceptar, admin, IP);

        assertThat(solicitud.getEstadoCodigo()).isEqualTo(Estado.ANAL_PEND);
        assertThat(solicitud.getResponsableAcademico()).isNull();
        verify(historial).save(argThat(cambio -> cambio.getUsuarioUltimoCambio() == admin));
        verifyNoInteractions(permisos);
    }

    @Test
    void elDuenioCancelaAntesDeEnviarYLaSolicitudQuedaCerrada() {
        Transicion transicion = motor.validar(solicitud, TransicionService.CANCELAR_SOLICITUD, Estado.SOL_INI, duenio);
        motor.aplicar(solicitud, transicion, duenio, IP);

        assertThat(solicitud.getEstadoCodigo()).isEqualTo(Estado.SOL_CANC);
        assertThat(motor.accionesDisponibles(solicitud, duenio)).isEmpty();
        verify(auditoria).registrarCambioEstado(duenio, null, Estado.SOL_INI, Estado.SOL_CANC, null, IP);
    }

    @Test
    void conLaDocumentacionEnviadaYaNoSeCancela() {
        solicitud.cambiarEstado(Estado.DOC_REV, OffsetDateTime.now());

        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.CANCELAR_SOLICITUD, Estado.DOC_REV, duenio),
                ErrorCode.TRANSICION_INVALIDA);
    }

    @Test
    void unRolSinEsaAccionRecibeAccesoDenegado() {
        Cuenta academico = mock(Cuenta.class);
        when(academico.tipo()).thenReturn(TitularTipo.USUARIO);
        when(academico.rolCodigo()).thenReturn(Rol.ACADEMICO);

        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, Estado.SOL_INI, academico),
                ErrorCode.ACCESO_DENEGADO);
    }

    @Test
    void otroAspiranteNoParticipa() {
        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, Estado.SOL_INI, aspirante(99)),
                ErrorCode.NO_PARTICIPANTE);
    }

    @Test
    void siElEstadoCambioLaAccionYaNoEstaDisponible() {
        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, Estado.DOC_REV, duenio),
                ErrorCode.TRANSICION_INVALIDA);
        verifyNoInteractions(historial, auditoria);
    }

    @Test
    void unaAccionQueNoSaleDelEstadoActualEsInvalida() {
        solicitud.cambiarEstado(Estado.DOC_REV, OffsetDateTime.now());

        assertCodigo(
                () -> motor.validar(solicitud, TransicionService.ENVIAR_DOCUMENTACION, Estado.DOC_REV, duenio),
                ErrorCode.TRANSICION_INVALIDA);
    }

    @Test
    void lasAccionesDisponiblesSalenDeLaTabla() {
        assertThat(motor.accionesDisponibles(solicitud, duenio))
                .containsExactly(TransicionService.ENVIAR_DOCUMENTACION, TransicionService.CANCELAR_SOLICITUD);
        assertThat(motor.accionesDisponibles(solicitud, aspirante(99))).isEmpty();

        solicitud.cambiarEstado(Estado.DOC_REV, OffsetDateTime.now());
        assertThat(motor.accionesDisponibles(solicitud, duenio)).isEmpty();
    }

    @Test
    void sinAccionesDelRolNiSeConsultanLosPermisos() {
        Cuenta academico = mock(Cuenta.class);
        when(academico.tipo()).thenReturn(TitularTipo.USUARIO);
        when(academico.rolCodigo()).thenReturn(Rol.ACADEMICO);

        assertThat(motor.accionesDisponibles(solicitud, academico)).isEmpty();
        verifyNoInteractions(permisos);
    }

    @Test
    void laCreacionQuedaComoPrimerCambio() {
        motor.registrarCreacion(solicitud, duenio, IP);

        verify(historial).save(argThat(cambio -> cambio.getEstadoAnteriorCodigo() == null
                && Estado.SOL_INI.equals(cambio.getEstadoCodigo())));
        verify(auditoria).registrarCambioEstado(any(), any(), any(), any(), any(), any());
    }

    private static Cuenta aspirante(int id) {
        Cuenta cuenta = mock(Cuenta.class);
        when(cuenta.tipo()).thenReturn(TitularTipo.ASPIRANTE);
        when(cuenta.rolCodigo()).thenReturn(Rol.ASPIRANTE);
        when(cuenta.getId()).thenReturn(id);
        return cuenta;
    }

    private static Solicitud solicitudDe(int aspiranteId) {
        Aspirante aspirante = mock(Aspirante.class);
        when(aspirante.getId()).thenReturn(aspiranteId);
        return new Solicitud(
                "EXP-2026-001", aspirante, new Carrera(), new DatosOrigen("UNLP", "Informática", "Sistemas", "REGULAR", null));
    }

    private static void assertCodigo(ThrowingCallable accion, ErrorCode esperado) {
        assertThatThrownBy(accion).isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.code())
                .isEqualTo(esperado));
    }
}
