package com.usal.e3.api.solicitudes;

import com.usal.e3.domain.HorarioPropuesto;
import com.usal.e3.domain.Reunion;
import com.usal.e3.service.ReunionService.Info;
import com.usal.e3.service.ReunionService.Ronda;
import java.time.OffsetDateTime;
import java.util.List;

/** `ReunionInfo` del contrato (§6.6): las rondas con sus horarios y la reunión vigente. */
public record ReunionInfoResponse(Integer rondaActual, List<RondaResponse> rondas, ReunionResponse reunion) {

    public record HorarioResponse(Integer id, OffsetDateTime inicio, OffsetDateTime fin, boolean elegido) {

        static HorarioResponse de(HorarioPropuesto horario) {
            return new HorarioResponse(horario.getId(), horario.getInicio(), horario.getFin(), horario.isElegido());
        }
    }

    public record RondaResponse(
            int numero, List<HorarioResponse> horarios, String motivoOtrasFechas, String disponibilidadAspirante) {

        static RondaResponse de(Ronda ronda) {
            return new RondaResponse(
                    ronda.ronda().getNumero(),
                    ronda.horarios().stream().map(HorarioResponse::de).toList(),
                    ronda.ronda().getMotivoOtrasFechas(),
                    ronda.ronda().getDisponibilidadAspirante());
        }
    }

    /** `notas` llega solo para el personal: son las que el Académico escribió al indicar que la reunión finalizó. */
    public record ReunionResponse(
            OffsetDateTime inicio, OffsetDateTime fin, String origen, String enlaceMeet, String resultado, String notas) {

        static ReunionResponse de(Reunion reunion, String notas) {
            return reunion == null
                    ? null
                    : new ReunionResponse(
                            reunion.getInicio(),
                            reunion.getFin(),
                            reunion.getOrigen().name(),
                            reunion.getEnlaceMeet(),
                            reunion.getResultado().name(),
                            notas);
        }
    }

    /** `rondaActual` es nulo hasta que el Académico propone la primera. */
    static ReunionInfoResponse de(Info info) {
        List<RondaResponse> rondas = info.rondas().stream().map(RondaResponse::de).toList();
        return new ReunionInfoResponse(
                rondas.isEmpty() ? null : rondas.get(rondas.size() - 1).numero(),
                rondas,
                ReunionResponse.de(info.reunion(), info.notas()));
    }
}
