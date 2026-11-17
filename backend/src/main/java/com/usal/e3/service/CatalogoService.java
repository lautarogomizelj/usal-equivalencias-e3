package com.usal.e3.service;

import com.usal.e3.domain.Carrera;
import com.usal.e3.domain.Estado;
import com.usal.e3.exception.ApiException;
import com.usal.e3.exception.ErrorCode;
import com.usal.e3.repository.CarreraRepository;
import com.usal.e3.repository.EstadoRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Catálogos que carga el backend y el frontend nunca hardcodea (contrato §5). */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final CarreraRepository carreras;
    private final EstadoRepository estados;

    public CatalogoService(CarreraRepository carreras, EstadoRepository estados) {
        this.carreras = carreras;
        this.estados = estados;
    }

    /** Las inactivas son baja lógica: solo el Admin las pide (`?activo=todas`). */
    public List<Carrera> carreras(boolean incluirInactivas) {
        return carreras.findAll(Sort.by("nombre")).stream()
                .filter(carrera -> incluirInactivas || carrera.isActivo())
                .toList();
    }

    public Carrera carrera(Integer id) {
        return carreras.findById(id).orElseThrow(() -> new ApiException(ErrorCode.NO_ENCONTRADO));
    }

    public List<Estado> estados() {
        return estados.findAllByOrderByIdAsc();
    }

    public Map<String, Estado> estadosPorCodigo() {
        return estados().stream().collect(Collectors.toMap(Estado::getCodigo, Function.identity()));
    }
}
