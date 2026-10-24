package haiderpabon.guias.usuariosrender.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import haiderpabon.guias.usuariosrender.datos.Usuario;

/**
 * Cliente HTTP de la API de usuarios desplegada en Render.
 *
 * POST   /api/auth/login   -> token JWT (publico)
 * POST   /api/users        -> crear usuario (publico)
 * GET    /api/users        -> listar (ADMIN o REVIEWER)
 * PUT    /api/users/{id}   -> actualizar (ADMIN)
 * DELETE /api/users/{id}   -> eliminar (ADMIN)
 */
public class ApiClient {

    public static final String URL_BASE = "https://users-management-render-haider-pabon.onrender.com";

    // Render (plan gratis) duerme la app y la primera peticion puede tardar casi un minuto
    private static final int TIEMPO_CONEXION_MS = 30_000;
    private static final int TIEMPO_LECTURA_MS = 75_000;

    private final String token;

    public ApiClient(String token) {
        this.token = token;
    }

    /** Respuesta del login: token y segundos de vigencia. */
    public static class Login {
        public final String token;
        public final long expiraEn;

        Login(String token, long expiraEn) {
            this.token = token;
            this.expiraEn = expiraEn;
        }
    }

    public Login login(String email, String password) throws IOException, ApiException, JSONException {
        JSONObject cuerpo = new JSONObject();
        cuerpo.put("email", email);
        cuerpo.put("password", password);
        JSONObject json = new JSONObject(enviar("POST", "/api/auth/login", cuerpo));
        return new Login(json.getString("accessToken"), json.optLong("expiresIn", 900));
    }

    public List<Usuario> listar() throws IOException, ApiException, JSONException {
        JSONArray arreglo = new JSONArray(enviar("GET", "/api/users", null));
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 0; i < arreglo.length(); i++) {
            usuarios.add(Usuario.desdeJson(arreglo.getJSONObject(i)));
        }
        return usuarios;
    }

    /** Crea el usuario con un id nuevo (UUID). La API lo deja en estado PENDING. */
    public Usuario crear(String nombre, String email, String password, String rol)
            throws IOException, ApiException, JSONException {
        JSONObject cuerpo = new JSONObject();
        cuerpo.put("id", UUID.randomUUID().toString());
        cuerpo.put("name", nombre);
        cuerpo.put("email", email);
        cuerpo.put("password", password);
        cuerpo.put("role", rol);
        return Usuario.desdeJson(new JSONObject(enviar("POST", "/api/users", cuerpo)));
    }

    /** Si password viene vacio la API conserva la contrasena actual. */
    public Usuario actualizar(Usuario usuario, String password)
            throws IOException, ApiException, JSONException {
        JSONObject cuerpo = new JSONObject();
        cuerpo.put("name", usuario.getNombre());
        cuerpo.put("email", usuario.getEmail());
        if (password != null && !password.isEmpty()) {
            cuerpo.put("password", password);
        }
        cuerpo.put("role", usuario.getRol());
        cuerpo.put("status", usuario.getEstado());
        return Usuario.desdeJson(new JSONObject(enviar("PUT", "/api/users/" + usuario.getId(), cuerpo)));
    }

    public void eliminar(String id) throws IOException, ApiException {
        enviar("DELETE", "/api/users/" + id, null);
    }

    private String enviar(String metodo, String ruta, JSONObject cuerpo) throws IOException, ApiException {
        HttpURLConnection conexion = (HttpURLConnection) new URL(URL_BASE + ruta).openConnection();
        try {
            conexion.setRequestMethod(metodo);
            conexion.setConnectTimeout(TIEMPO_CONEXION_MS);
            conexion.setReadTimeout(TIEMPO_LECTURA_MS);
            conexion.setRequestProperty("Accept", "application/json");
            if (token != null) {
                conexion.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (cuerpo != null) {
                byte[] datos = cuerpo.toString().getBytes(StandardCharsets.UTF_8);
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conexion.setFixedLengthStreamingMode(datos.length);
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(datos);
                }
            }

            int codigo = conexion.getResponseCode();
            InputStream entrada = codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            String texto = leer(entrada);
            if (codigo >= 400) {
                throw new ApiException(codigo, mensajeDeError(codigo, texto));
            }
            return texto;
        } catch (SocketTimeoutException e) {
            throw new IOException("El servidor tardo demasiado en responder.", e);
        } finally {
            conexion.disconnect();
        }
    }

    private static String leer(InputStream entrada) throws IOException {
        if (entrada == null) {
            return "";
        }
        try (InputStream in = entrada; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, n);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    /** Usa el campo "message" de ApiErrorResponse; algunos vienen en ingles y se traducen. */
    private static String mensajeDeError(int codigo, String texto) {
        String mensaje = "";
        try {
            mensaje = new JSONObject(texto).optString("message", "");
        } catch (JSONException ignored) {
            // la respuesta no era JSON
        }
        switch (codigo) {
            case 403:
                return "No tienes permisos para realizar esta operacion.";
            case 404:
                return "El usuario no existe.";
            case 409:
                return "Ya existe un usuario con ese correo.";
            default:
                if (!mensaje.isEmpty()) {
                    return mensaje;
                }
                return "Error " + codigo + " del servidor.";
        }
    }
}
