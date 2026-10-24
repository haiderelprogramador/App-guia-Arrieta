package haiderpabon.guias.usuariosrender.api;

/** Error devuelto por la API (codigo HTTP y mensaje del campo "message"). */
public class ApiException extends Exception {

    private final int codigo;

    public ApiException(int codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
