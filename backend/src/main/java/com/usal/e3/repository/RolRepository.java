package com.usal.e3.repository;

import com.usal.e3.domain.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByCodigo(String codigo);
}
