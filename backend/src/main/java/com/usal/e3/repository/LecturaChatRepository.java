package com.usal.e3.repository;

import com.usal.e3.domain.LecturaChat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturaChatRepository extends JpaRepository<LecturaChat, Integer> {

    /** A lo sumo tres: el aspirante y los dos responsables. */
    List<LecturaChat> findBySolicitudId(Integer solicitudId);
}
