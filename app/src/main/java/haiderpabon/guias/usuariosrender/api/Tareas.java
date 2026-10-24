package haiderpabon.guias.usuariosrender.api;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ejecuta las peticiones en un hilo aparte (Android no deja usar la red en el hilo principal)
 * y entrega el resultado en el hilo de la interfaz. Reemplaza a AsyncTask, que esta obsoleto.
 */
public final class Tareas {

    private static final ExecutorService HILOS = Executors.newFixedThreadPool(3);
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Trabajo<T> {
        T ejecutar() throws Exception;
    }

    public interface Respuesta<T> {
        /** Si error es null la peticion salio bien y valor tiene el resultado. */
        void listo(T valor, Exception error);
    }

    private Tareas() {
    }

    public static <T> void ejecutar(final Trabajo<T> trabajo, final Respuesta<T> respuesta) {
        HILOS.execute(() -> {
            try {
                final T valor = trabajo.ejecutar();
                PRINCIPAL.post(() -> respuesta.listo(valor, null));
            } catch (final Exception e) {
                PRINCIPAL.post(() -> respuesta.listo(null, e));
            }
        });
    }
}
