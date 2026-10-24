package haiderpabon.guias.usuariosrender.vistas;

import java.io.IOException;

import haiderpabon.guias.usuariosrender.api.ApiException;

/** Convierte los errores de las peticiones en mensajes para el usuario. */
final class Mensajes {

    private Mensajes() {
    }

    static String de(Exception error) {
        if (error instanceof ApiException) {
            return error.getMessage();
        }
        if (error instanceof IOException) {
            return "No se pudo conectar con el servidor. Revisa tu internet; si la app en Render "
                    + "estaba dormida puede tardar cerca de un minuto, intenta otra vez.";
        }
        return "Ocurrio un error inesperado: " + error.getMessage();
    }

    static boolean sesionVencida(Exception error) {
        return error instanceof ApiException && ((ApiException) error).getCodigo() == 401;
    }
}
