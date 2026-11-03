package com.usal.e3.repository;

import com.usal.e3.domain.Aspirante;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AspiranteRepository extends JpaRepository<Aspirante, Integer> {

    Optional<Aspirante> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    /** Con bloqueo de fila: dos intentos simultáneos no pueden perder un fallo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aspirante a where a.email = :email")
    Optional<Aspirante> buscarParaLogin(String email);
}
