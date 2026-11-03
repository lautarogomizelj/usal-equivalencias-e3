package com.usal.e3.repository;

import com.usal.e3.domain.Carrera;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    Optional<Carrera> findByCodigo(String codigo);
}
