package com.usal.e3.repository;

import com.usal.e3.domain.Solicitud;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface SolicitudRepository extends JpaRepository<Solicitud, Integer> {

    List<Solicitud> findByAspiranteId(Integer aspiranteId);

    /** Con bloqueo de fila: dos acciones simultáneas no pueden partir del mismo estado. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Solicitud s where s.id = :id")
    Optional<Solicitud> buscarParaActualizar(Integer id);

    @Query(value = "select nextval('solicitudes_numero_exp_seq')", nativeQuery = true)
    long siguienteNumeroExp();
}
