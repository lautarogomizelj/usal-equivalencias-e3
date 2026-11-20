package com.usal.e3.repository;

import com.usal.e3.domain.Usuario;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Con bloqueo de fila: dos logins simultáneos no pueden perder un intento fallido. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> buscarParaLogin(String email);

    /** Personal activo de un rol con permiso permanente vigente sobre la carrera (RF-PROC-003, RF-PROC-004). */
    @Query("""
            select u from Usuario u
            where u.activo = true and u.rol.codigo = :rolCodigo
              and exists (select p from PermisoCarrera p
                          where p.userId = u.id and p.carrera.id = :carreraId and p.revokedAt is null)
            order by u.id
            """)
    List<Usuario> conPermisoEnCarrera(Integer carreraId, String rolCodigo);
}
