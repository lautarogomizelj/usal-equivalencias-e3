package com.usal.e3.service;

import com.usal.e3.domain.Aspirante;
import com.usal.e3.domain.PermisoCarrera;
import com.usal.e3.domain.Rol;
import com.usal.e3.domain.Usuario;
import com.usal.e3.repository.AspiranteRepository;
import com.usal.e3.repository.CarreraRepository;
import com.usal.e3.repository.PermisoCarreraRepository;
import com.usal.e3.repository.RolRepository;
import com.usal.e3.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Una cuenta por rol para la demo (README, "Datos Demo"). Idempotente; apagado por defecto. */
@Component
@ConditionalOnProperty(name = "e3.seed-demo", havingValue = "true")
public class SeedDemo implements ApplicationRunner {

    private static final String PASSWORD_DEMO = "Demo1234!";
    private static final String CARRERA_DEMO = "ING-INF-001";

    private final UsuarioRepository usuarios;
    private final AspiranteRepository aspirantes;
    private final RolRepository roles;
    private final CarreraRepository carreras;
    private final PermisoCarreraRepository permisos;
    private final PasswordEncoder passwordEncoder;

    public SeedDemo(
            UsuarioRepository usuarios,
            AspiranteRepository aspirantes,
            RolRepository roles,
            CarreraRepository carreras,
            PermisoCarreraRepository permisos,
            PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.aspirantes = aspirantes;
        this.roles = roles;
        this.carreras = carreras;
        this.permisos = permisos;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        crearUsuario("admin@usal.edu.ar", "Admin Demo", Rol.ADMIN, false);
        crearUsuario("academico@usal.edu.ar", "Académico Demo", Rol.ACADEMICO, true);
        crearUsuario("secretaria@usal.edu.ar", "Secretaría Demo", Rol.SECRETARIA, true);
        crearUsuario("revisor@usal.edu.ar", "Revisor Demo", Rol.REVISOR, false);
        // Observador no es un rol: es un Académico sin permiso de proceso (RF-AUTH-005).
        crearUsuario("observador@usal.edu.ar", "Observador Demo", Rol.ACADEMICO, false);
        if (!aspirantes.existsByEmail("aspirante@example.com")) {
            Aspirante aspirante = new Aspirante(
                    "aspirante@example.com",
                    passwordEncoder.encode(PASSWORD_DEMO),
                    "Aspirante Demo",
                    "+54 11 5555-0000",
                    "40123456");
            aspirante.setEmailValidado(true);
            aspirantes.save(aspirante);
        }
    }

    private void crearUsuario(String email, String nombre, String rolCodigo, boolean conCarrera) {
        if (usuarios.existsByEmail(email)) {
            return;
        }
        Usuario usuario = new Usuario(email, nombre, roles.findByCodigo(rolCodigo).orElseThrow());
        if (Rol.ADMIN.equals(rolCodigo)) {
            usuario.setPasswordHash(passwordEncoder.encode(PASSWORD_DEMO));
        }
        usuarios.save(usuario);
        if (conCarrera) {
            permisos.save(new PermisoCarrera(usuario.getId(), carreras.findByCodigo(CARRERA_DEMO).orElseThrow()));
        }
    }
}
