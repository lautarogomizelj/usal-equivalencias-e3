package com.usal.e3.api.usuarios;

import com.usal.e3.domain.Cuenta;
import com.usal.e3.service.PerfilService;
import java.util.List;

/** `Perfil` del contrato (§4.2). */
public record PerfilResponse(
        String nombre,
        String email,
        String telefono,
        String dni,
        String avatar,
        String rol,
        boolean esOAuth,
        List<String> camposEditables) {

    static PerfilResponse de(Cuenta cuenta) {
        return new PerfilResponse(
                cuenta.getNombre(),
                cuenta.getEmail(),
                cuenta.getTelefono(),
                cuenta.getDni(),
                cuenta.getAvatar(),
                cuenta.rolCodigo(),
                cuenta.esOAuth(),
                PerfilService.camposEditables(cuenta));
    }
}
