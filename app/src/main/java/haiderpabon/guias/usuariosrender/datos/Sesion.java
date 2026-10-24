package haiderpabon.guias.usuariosrender.datos;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Guarda el token JWT y los datos del usuario que inicio sesion.
 * El rol y el id se leen del propio token (campos "role" y "sub").
 */
public class Sesion {

    private static final String ARCHIVO = "sesion";
    private final SharedPreferences prefs;

    public Sesion(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public void guardar(String token, long expiraEnSegundos, String email) {
        String id = "";
        String rol = "";
        try {
            String[] partes = token.split("\\.");
            byte[] datos = Base64.decode(partes[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            JSONObject payload = new JSONObject(new String(datos, StandardCharsets.UTF_8));
            id = payload.optString("sub", "");
            rol = payload.optString("role", "");
        } catch (Exception ignored) {
            // si el token no se puede leer, la API igual lo valida en cada peticion
        }
        prefs.edit()
                .putString("token", token)
                .putString("id", id)
                .putString("rol", rol)
                .putString("email", email)
                .putLong("expira", System.currentTimeMillis() + expiraEnSegundos * 1000L)
                .apply();
    }

    public boolean activa() {
        return getToken() != null && System.currentTimeMillis() < prefs.getLong("expira", 0);
    }

    public void cerrar() {
        prefs.edit().clear().apply();
    }

    public String getToken() { return prefs.getString("token", null); }
    public String getId() { return prefs.getString("id", ""); }
    public String getRol() { return prefs.getString("rol", ""); }
    public String getEmail() { return prefs.getString("email", ""); }

    public boolean esAdmin() { return "ADMIN".equals(getRol()); }
    public boolean puedeVerListado() { return esAdmin() || "REVIEWER".equals(getRol()); }
}
