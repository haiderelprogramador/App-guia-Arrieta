package haiderpabon.guias.usuariosrender.datos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/** Usuario tal como lo devuelve la API (UserRestResponse). */
public class Usuario implements Serializable {

    public static final String[] ROLES = {"MEMBER", "REVIEWER", "ADMIN"};
    public static final String[] ESTADOS = {"ACTIVE", "PENDING", "INACTIVE", "BLOCKED"};

    private String id;
    private String nombre;
    private String email;
    private String rol;
    private String estado;

    public Usuario() {
    }

    public Usuario(String id, String nombre, String email, String rol, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
    }

    public static Usuario desdeJson(JSONObject json) throws JSONException {
        return new Usuario(
                json.getString("id"),
                json.getString("name"),
                json.getString("email"),
                json.getString("role"),
                json.optString("status", ""));
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public String getEstado() { return estado; }

    public void setId(String id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setRol(String rol) { this.rol = rol; }
    public void setEstado(String estado) { this.estado = estado; }
}
