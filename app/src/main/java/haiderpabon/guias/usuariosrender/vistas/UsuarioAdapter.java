package haiderpabon.guias.usuariosrender.vistas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import haiderpabon.guias.usuariosrender.R;
import haiderpabon.guias.usuariosrender.datos.Usuario;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.Fila> {

    public interface AlTocar {
        void usuarioTocado(Usuario usuario);
    }

    private final List<Usuario> usuarios = new ArrayList<>();
    private final AlTocar alTocar;

    public UsuarioAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void mostrar(List<Usuario> nuevos) {
        usuarios.clear();
        usuarios.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        final Usuario u = usuarios.get(posicion);
        fila.lblInicial.setText(u.getNombre().isEmpty() ? "?" : u.getNombre().substring(0, 1).toUpperCase());
        fila.lblNombre.setText(u.getNombre());
        fila.lblEmail.setText(u.getEmail());
        fila.lblRol.setText(u.getRol());
        fila.lblEstado.setText(u.getEstado());
        fila.lblEstado.setTextColor(ContextCompat.getColor(fila.itemView.getContext(), colorEstado(u.getEstado())));
        fila.itemView.setOnClickListener(v -> alTocar.usuarioTocado(u));
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    private static int colorEstado(String estado) {
        if ("ACTIVE".equals(estado)) {
            return R.color.estado_activo;
        }
        if ("PENDING".equals(estado)) {
            return R.color.estado_pendiente;
        }
        return R.color.estado_inactivo;
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView lblInicial;
        final TextView lblNombre;
        final TextView lblEmail;
        final TextView lblRol;
        final TextView lblEstado;

        Fila(View vista) {
            super(vista);
            lblInicial = vista.findViewById(R.id.lblInicial);
            lblNombre = vista.findViewById(R.id.lblNombre);
            lblEmail = vista.findViewById(R.id.lblEmail);
            lblRol = vista.findViewById(R.id.lblRol);
            lblEstado = vista.findViewById(R.id.lblEstado);
        }
    }
}
