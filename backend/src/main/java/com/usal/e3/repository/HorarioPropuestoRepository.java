package com.usal.e3.repository;

import com.usal.e3.domain.HorarioPropuesto;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioPropuestoRepository extends JpaRepository<HorarioPropuesto, Integer> {

    List<HorarioPropuesto> findByRondaIdInOrderByInicioAsc(Collection<Integer> rondaIds);
}
