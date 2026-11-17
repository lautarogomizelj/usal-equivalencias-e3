package com.usal.e3.api.solicitudes;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** `Page<T>` del contrato (§1). */
public record PaginaResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    static <S, T> PaginaResponse<T> de(Page<S> pagina, Function<S, T> mapear) {
        return new PaginaResponse<>(
                pagina.getContent().stream().map(mapear).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
