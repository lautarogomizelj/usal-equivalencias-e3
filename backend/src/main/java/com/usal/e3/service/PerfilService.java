package com.usal.e3.service;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.Rol;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.UsuarioRepository;
import com.usal.e3.security.SesionActual;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Mi perfil (RF-AUTH-007). Todos los cambios quedan en auditoría. */
@Service
public class PerfilService {

    private final CuentaService cuentas;
    private final UsuarioRepository usuarios;
    private final SesionService sesiones;
    private final AuditoriaService auditoria;
    private final PasswordEncoder passwordEncoder;

    public PerfilService(
            CuentaService cuentas,
            UsuarioRepository usuarios,
            SesionService sesiones,
            AuditoriaService auditoria,
            PasswordEncoder passwordEncoder) {
        this.cuentas = cuentas;
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.auditoria = auditoria;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Cuenta obtener(SesionActual actual) {
        return cuenta(actual);
    }

    public static List<String> camposEditables(Cuenta cuenta) {
        if (Rol.REVISOR.equals(cuenta.rolCodigo())) {
            return List.of();
        }
        List<String> campos = new ArrayList<>(List.of("telefono", "avatar"));
        if (!cuenta.esOAuth()) {
            campos.add(0, "nombre");
        }
        if (Rol.ADMIN.equals(cuenta.rolCodigo())) {
            campos.add("email");
        }
        return campos;
    }

    @Transactional
    public Cuenta editar(SesionActual actual, String nombre, String telefono, String avatar, String ip) {
        Cuenta cuenta = cuenta(actual);
        if (nombre != null) {
            if (cuenta.esOAuth()) {
                throw ApiException.enCampo(ErrorCode.VALIDACION, "nombre", "El nombre viene de tu cuenta de Google.");
            }
            cuenta.setNombre(nombre.trim());
        }
        if (telefono != null) {
            cuenta.setTelefono(telefono.trim());
        }
        if (avatar != null) {
            cuenta.setAvatar(avatar);
        }
        auditoria.registrar(cuenta, "cambio_perfil", null, ip);
        return cuenta;
    }

    @Transactional
    public void cambiarPassword(SesionActual actual, String passwordActual, String passwordNueva, String ip) {
        Cuenta cuenta = cuenta(actual);
        exigirPassword(cuenta, passwordActual);
        cuenta.setPasswordHash(passwordEncoder.encode(passwordNueva));
        sesiones.cerrarOtras(actual);
        auditoria.registrar(cuenta, "cambio_password", null, ip);
    }

    @Transactional
    public void cambiarEmail(SesionActual actual, String emailNuevo, String passwordActual, String ip) {
        Cuenta cuenta = cuenta(actual);
        exigirPassword(cuenta, passwordActual);
        if (!emailNuevo.endsWith("@" + AuthService.DOMINIO_INSTITUCIONAL)) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "emailNuevo", "El email debe ser @usal.edu.ar.");
        }
        if (!emailNuevo.equals(cuenta.getEmail()) && usuarios.existsByEmail(emailNuevo)) {
            throw ApiException.enCampo(ErrorCode.DUPLICADO, "emailNuevo", "Ya existe una cuenta con este email.");
        }
        String anterior = cuenta.getEmail();
        cuenta.setEmail(emailNuevo);
        auditoria.registrar(cuenta, "cambio_email", Map.of("anterior", anterior, "nuevo", emailNuevo), ip);
    }

    private void exigirPassword(Cuenta cuenta, String password) {
        if (!passwordEncoder.matches(password, cuenta.getPasswordHash())) {
            throw ApiException.enCampo(ErrorCode.VALIDACION, "passwordActual", "La contraseña actual no es correcta.");
        }
    }

    private Cuenta cuenta(SesionActual actual) {
        return cuentas.buscar(actual.tipo(), actual.id()).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }
}
