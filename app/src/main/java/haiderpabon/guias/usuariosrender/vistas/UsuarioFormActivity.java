package haiderpabon.guias.usuariosrender.vistas;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

import haiderpabon.guias.usuariosrender.R;
import haiderpabon.guias.usuariosrender.api.ApiClient;
import haiderpabon.guias.usuariosrender.api.Tareas;
import haiderpabon.guias.usuariosrender.datos.Sesion;
import haiderpabon.guias.usuariosrender.datos.Usuario;

/**
 * Crear (POST /api/users), editar (PUT /api/users/{id}) y eliminar (DELETE /api/users/{id}).
 * Solo lo usa el rol ADMIN.
 */
public class UsuarioFormActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO = "usuario";

    private Usuario usuario; // null = usuario nuevo
    private Sesion sesion;

    private TextInputEditText txtNombre;
    private TextInputEditText txtEmail;
    private TextInputEditText txtPassword;
    private Spinner spnRol;
    private Spinner spnEstado;
    private Button btnGuardar;
    private Button btnEliminar;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_form);
        sesion = new Sesion(this);
        usuario = (Usuario) getIntent().getSerializableExtra(EXTRA_USUARIO);
        boolean esNuevo = usuario == null;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(esNuevo ? "Nuevo usuario" : "Editar usuario");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txtNombre = findViewById(R.id.txtNombre);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        spnRol = findViewById(R.id.spnRol);
        spnEstado = findViewById(R.id.spnEstado);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        progreso = findViewById(R.id.progreso);
        TextInputLayout capaPassword = findViewById(R.id.capaPassword);
        TextView lblId = findViewById(R.id.lblId);

        spnRol.setAdapter(adaptador(Usuario.ROLES));
        spnEstado.setAdapter(adaptador(Usuario.ESTADOS));

        if (esNuevo) {
            capaPassword.setHelperText("Minimo 8 caracteres");
            lblId.setVisibility(View.GONE);
            btnEliminar.setVisibility(View.GONE);
        } else {
            capaPassword.setHelperText("Dejala vacia para conservar la contrasena actual");
            lblId.setText("Id: " + usuario.getId());
            txtNombre.setText(usuario.getNombre());
            txtEmail.setText(usuario.getEmail());
            spnRol.setSelection(Math.max(0, Arrays.asList(Usuario.ROLES).indexOf(usuario.getRol())));
            spnEstado.setSelection(Math.max(0, Arrays.asList(Usuario.ESTADOS).indexOf(usuario.getEstado())));

            // Para no quedarse sin acceso, el admin no puede cambiarse el rol, el estado ni borrarse
            if (usuario.getId().equals(sesion.getId())) {
                spnRol.setEnabled(false);
                spnEstado.setEnabled(false);
                btnEliminar.setVisibility(View.GONE);
                findViewById(R.id.lblPropio).setVisibility(View.VISIBLE);
            }
        }

        btnGuardar.setOnClickListener(v -> guardar());
        btnEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private ArrayAdapter<String> adaptador(String[] opciones) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opciones);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void guardar() {
        final String nombre = LoginActivity.texto(txtNombre);
        final String email = LoginActivity.texto(txtEmail);
        final String password = LoginActivity.texto(txtPassword);
        final String rol = (String) spnRol.getSelectedItem();
        final String estado = (String) spnEstado.getSelectedItem();
        final boolean esNuevo = usuario == null;

        if (nombre.length() < 3) {
            txtNombre.setError("Minimo 3 caracteres");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Escribe un correo valido");
            return;
        }
        if ((esNuevo || !password.isEmpty()) && password.length() < 8) {
            txtPassword.setError("Minimo 8 caracteres");
            return;
        }

        final ApiClient api = new ApiClient(sesion.getToken());
        cargando(true);
        Tareas.ejecutar(() -> {
            if (esNuevo) {
                Usuario creado = api.crear(nombre, email, password, rol);
                // La API crea los usuarios en PENDING; si el admin eligio otro estado se actualiza
                if (!"PENDING".equals(estado)) {
                    creado.setEstado(estado);
                    creado = api.actualizar(creado, null);
                }
                return creado;
            }
            Usuario cambios = new Usuario(usuario.getId(), nombre, email, rol, estado);
            return api.actualizar(cambios, password);
        }, (guardado, error) -> {
            if (isFinishing()) {
                return;
            }
            cargando(false);
            if (error != null) {
                Snackbar.make(btnGuardar, Mensajes.de(error), Snackbar.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, esNuevo ? "Usuario creado" : "Usuario actualizado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar usuario")
                .setMessage("¿Seguro que quieres eliminar a " + usuario.getNombre() + "? No se puede deshacer.")
                .setPositiveButton("Eliminar", (d, w) -> eliminar())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminar() {
        final ApiClient api = new ApiClient(sesion.getToken());
        final String id = usuario.getId();
        cargando(true);
        Tareas.ejecutar(() -> {
            api.eliminar(id);
            return Boolean.TRUE;
        }, (ok, error) -> {
            if (isFinishing()) {
                return;
            }
            cargando(false);
            if (error != null) {
                Snackbar.make(btnGuardar, Mensajes.de(error), Snackbar.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void cargando(boolean activo) {
        progreso.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!activo);
        btnEliminar.setEnabled(!activo);
    }
}
