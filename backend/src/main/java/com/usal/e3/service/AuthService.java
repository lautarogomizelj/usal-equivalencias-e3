package com.usal.e3.service;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.Cuenta;
import com.usal.e3.domain.PropositoCodigo;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.TitularTipo;
import com.usal.e3.domain.Usuario;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.UsuarioRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro y logins (RF-AUTH-001 a 004). Los errores de login usan `noRollbackFor`: si no, deshacer la
 * transacción borraría el intento fallido que se acaba de contar y el bloqueo nunca se activaría.
 */
@Service
public class AuthService {

    public static final String DOMINIO_INSTITUCIONAL = "usal.edu.ar";

    private final AspiranteRepository aspirantes;
    private final UsuarioRepository usuarios;
    private final CodigoService codigos;
    private final SesionService sesiones;
    private final MailService mail;
    private final AuditoriaService auditoria;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AspiranteRepository aspirantes,
            UsuarioRepository usuarios,
            CodigoService codigos,
            SesionService sesiones,
            MailService mail,
            AuditoriaService auditoria,
            PasswordEncoder passwordEncoder) {
        this.aspirantes = aspirantes;
        this.usuarios = usuarios;
        this.codigos = codigos;
        this.sesiones = sesiones;
        this.mail = mail;
        this.auditoria = auditoria;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Aspirante registrar(
            String email, String password, String nombre, String telefono, String dni, ClienteHttp cliente) {
        if (aspirantes.existsByEmail(email)) {
            throw ApiException.enCampo(ErrorCode.DUPLICADO, "email", "Ya existe una cuenta con este email.");
        }
        if (aspirantes.existsByDni(dni)) {
            throw ApiException.enCampo(ErrorCode.DUPLICADO, "dni", "Ya existe una cuenta con este DNI.");
        }
        Aspirante aspirante =
                aspirantes.save(new Aspirante(email, passwordEncoder.encode(password), nombre, telefono, dni));
        auditoria.registrar(aspirante, "crear_cuenta", null, cliente.ip());
        mail.enviarCodigoValidacion(email, codigos.generar(aspirante, PropositoCodigo.VALIDACION_EMAIL));
        return aspirante;
    }

    @Transactional(noRollbackFor = ApiException.class)
    public SesionIniciada validarRegistro(String email, String codigo, ClienteHttp cliente) {
        Aspirante aspirante = aspirantes.buscarParaLogin(email)
                .filter(a -> !a.isEmailValidado())
                .orElseThrow(() -> new ApiException(ErrorCode.CODIGO_INVALIDO));
        codigos.verificar(aspirante, PropositoCodigo.VALIDACION_EMAIL, codigo);
        aspirante.setEmailValidado(true);
        BloqueoCuenta.limpiar(aspirante);
        auditoria.registrar(aspirante, "validar_email", null, cliente.ip());
        return iniciarSesion(aspirante, SesionService.NORMAL, cliente);
    }

    /** Sin error si la cuenta no existe o ya está validada, para no revelar qué emails hay. */
    @Transactional
    public void reenviarCodigo(String email) {
        aspirantes.buscarParaLogin(email)
                .filter(a -> !a.isEmailValidado())
                .ifPresent(a -> mail.enviarCodigoValidacion(
                        email, codigos.generar(a, PropositoCodigo.VALIDACION_EMAIL)));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public SesionIniciada loginAspirante(String email, String password, boolean recordarme, ClienteHttp cliente) {
        Aspirante aspirante = aspirantes.buscarParaLogin(email)
                .filter(Aspirante::isActivo)
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENCIALES_INVALIDAS));
        comprobarPassword(aspirante, password, cliente);
        if (!aspirante.isEmailValidado()) {
            throw new ApiException(ErrorCode.EMAIL_NO_VALIDADO);
        }
        return iniciarSesion(aspirante, recordarme ? SesionService.RECORDARME : SesionService.NORMAL, cliente);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public SesionIniciada loginAdmin(String usuario, String password, ClienteHttp cliente) {
        Usuario admin = usuarios.buscarParaLogin(usuario)
                .filter(u -> u.isActivo() && Rol.ADMIN.equals(u.rolCodigo()))
                .orElseThrow(() -> new ApiException(ErrorCode.CREDENCIALES_INVALIDAS));
        comprobarPassword(admin, password, cliente);
        return iniciarSesion(admin, SesionService.NORMAL, cliente);
    }

    /** RF-AUTH-003: dominio @usal.edu.ar, alta previa del Admin y cuenta activa. El Admin no entra por acá. */
    @Transactional
    public SesionIniciada loginGoogle(String email, String sub, boolean emailVerificado, ClienteHttp cliente) {
        ApiException noRegistrado = new ApiException(ErrorCode.USUARIO_NO_REGISTRADO);
        if (email == null || !emailVerificado || !email.toLowerCase().endsWith("@" + DOMINIO_INSTITUCIONAL)) {
            throw noRegistrado;
        }
        Usuario usuario = usuarios.findByEmail(email.toLowerCase())
                .filter(u -> u.isActivo() && u.esOAuth())
                .orElseThrow(() -> noRegistrado);
        if (usuario.getGoogleSub() != null && !usuario.getGoogleSub().equals(sub)) {
            throw noRegistrado;
        }
        usuario.setGoogleSub(sub);
        return iniciarSesion(usuario, SesionService.NORMAL, cliente);
    }

    /** Login simulado del Sprint Planning hasta tener credenciales de Google. Solo existe en perfil dev. */
    @Transactional
    public SesionIniciada loginDesarrollo(String email, ClienteHttp cliente) {
        Usuario usuario = usuarios.findByEmail(email)
                .filter(u -> u.isActivo() && u.esOAuth())
                .orElseThrow(() -> new ApiException(ErrorCode.USUARIO_NO_REGISTRADO));
        return iniciarSesion(usuario, SesionService.NORMAL, cliente);
    }

    /** Responde igual exista o no el email (contrato §4.1). */
    @Transactional
    public void recuperarPassword(String email) {
        aspirantes.buscarParaLogin(email)
                .filter(Aspirante::isActivo)
                .ifPresent(a -> mail.enviarCodigoRecupero(
                        email, codigos.generar(a, PropositoCodigo.RECUPERO_PASSWORD)));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public void restablecerPassword(String email, String codigo, String passwordNueva, ClienteHttp cliente) {
        Aspirante aspirante = aspirantes.buscarParaLogin(email)
                .filter(Aspirante::isActivo)
                .orElseThrow(() -> new ApiException(ErrorCode.CODIGO_INVALIDO));
        codigos.verificar(aspirante, PropositoCodigo.RECUPERO_PASSWORD, codigo);
        aspirante.setPasswordHash(passwordEncoder.encode(passwordNueva));
        BloqueoCuenta.limpiar(aspirante);
        sesiones.cerrarTodas(TitularTipo.ASPIRANTE, aspirante.getId());
        auditoria.registrar(aspirante, "recupero_password", null, cliente.ip());
    }

    private void comprobarPassword(Cuenta cuenta, String password, ClienteHttp cliente) {
        OffsetDateTime ahora = OffsetDateTime.now();
        BloqueoCuenta.verificar(cuenta, ahora);
        if (!passwordEncoder.matches(password, cuenta.getPasswordHash())) {
            ApiException error = BloqueoCuenta.registrarFallo(cuenta, ahora);
            if (cuenta.getBloqueadoHasta() != null) {
                auditoria.registrar(cuenta, "bloqueo_cuenta", Map.of("motivo", "intentos_login"), cliente.ip());
            }
            throw error;
        }
        BloqueoCuenta.limpiar(cuenta);
    }

    private SesionIniciada iniciarSesion(Cuenta cuenta, Duration duracion, ClienteHttp cliente) {
        SesionIniciada sesion = sesiones.iniciar(cuenta, duracion, cliente);
        auditoria.registrar(cuenta, "login", null, cliente.ip());
        return sesion;
    }
}
