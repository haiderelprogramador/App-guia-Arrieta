package haiderpabon.guias.usuariosrender.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import haiderpabon.guias.usuariosrender.R;
import haiderpabon.guias.usuariosrender.api.ApiClient;
import haiderpabon.guias.usuariosrender.api.Tareas;
import haiderpabon.guias.usuariosrender.datos.Sesion;
import haiderpabon.guias.usuariosrender.datos.Usuario;

/** Listado de usuarios (GET /api/users). ADMIN puede crear, editar y eliminar; REVIEWER solo ver. */
public class UsuariosActivity extends AppCompatActivity {

    private Sesion sesion;
    private UsuarioAdapter adapter;
    private SwipeRefreshLayout refrescar;
    private TextView lblVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);
        sesion = new Sesion(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Usuarios");
        }

        TextView lblSesion = findViewById(R.id.lblSesion);
        lblSesion.setText(sesion.getEmail() + "  ·  " + sesion.getRol());

        lblVacio = findViewById(R.id.lblVacio);
        refrescar = findViewById(R.id.refrescar);
        refrescar.setOnRefreshListener(this::cargar);

        RecyclerView lista = findViewById(R.id.lista);
        lista.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuarioAdapter(this::usuarioTocado);
        lista.setAdapter(adapter);

        FloatingActionButton btnNuevo = findViewById(R.id.btnNuevo);
        btnNuevo.setVisibility(sesion.esAdmin() ? View.VISIBLE : View.GONE);
        btnNuevo.setOnClickListener(v -> startActivity(new Intent(this, UsuarioFormActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sesion.activa()) {
            irAlLogin("Tu sesion expiro, vuelve a iniciar sesion.");
            return;
        }
        cargar();
    }

    private void cargar() {
        if (!sesion.puedeVerListado()) {
            refrescar.setRefreshing(false);
            mostrarVacio("Tu rol es " + sesion.getRol() + ". Solo los usuarios ADMIN y REVIEWER "
                    + "pueden ver el listado de usuarios.");
            return;
        }
        refrescar.setRefreshing(true);
        Tareas.ejecutar(() -> new ApiClient(sesion.getToken()).listar(), (usuarios, error) -> {
            if (isFinishing()) {
                return;
            }
            refrescar.setRefreshing(false);
            if (error != null) {
                if (Mensajes.sesionVencida(error)) {
                    irAlLogin("Tu sesion expiro, vuelve a iniciar sesion.");
                    return;
                }
                mostrarVacio(Mensajes.de(error) + "\n\nDesliza hacia abajo para reintentar.");
                return;
            }
            if (usuarios.isEmpty()) {
                mostrarVacio("No hay usuarios registrados.");
            } else {
                adapter.mostrar(usuarios);
                lblVacio.setVisibility(View.GONE);
            }
        });
    }

    private void mostrarVacio(String mensaje) {
        adapter.mostrar(new ArrayList<>());
        lblVacio.setText(mensaje);
        lblVacio.setVisibility(View.VISIBLE);
    }

    private void usuarioTocado(Usuario usuario) {
        if (sesion.esAdmin()) {
            Intent intent = new Intent(this, UsuarioFormActivity.class);
            intent.putExtra(UsuarioFormActivity.EXTRA_USUARIO, usuario);
            startActivity(intent);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(usuario.getNombre())
                .setMessage("Correo: " + usuario.getEmail()
                        + "\nRol: " + usuario.getRol()
                        + "\nEstado: " + usuario.getEstado()
                        + "\nId: " + usuario.getId()
                        + "\n\nSolo un ADMIN puede editar usuarios.")
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void irAlLogin(String mensaje) {
        sesion.cerrar();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_usuarios, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.accionActualizar) {
            cargar();
            return true;
        }
        if (item.getItemId() == R.id.accionSalir) {
            irAlLogin("Sesion cerrada.");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
