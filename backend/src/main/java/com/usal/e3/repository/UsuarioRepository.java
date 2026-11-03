package com.usal.e3.repository;

import com.usal.e3.domain.Usuario;
import jakarta.persistence.LockModeType;
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
}
