package haiderpabon.guias.usuariosrender.vistas;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import haiderpabon.guias.usuariosrender.R;
import haiderpabon.guias.usuariosrender.api.ApiClient;
import haiderpabon.guias.usuariosrender.api.Tareas;

/** Registro publico: crea una cuenta MEMBER que queda pendiente hasta que un ADMIN la active. */
public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText txtNombre;
    private TextInputEditText txtEmail;
    private TextInputEditText txtPassword;
    private TextInputEditText txtConfirmar;
    private Button btnRegistrar;
    private ProgressBar progreso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Crear cuenta");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txtNombre = findViewById(R.id.txtNombre);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtConfirmar = findViewById(R.id.txtConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progreso = findViewById(R.id.progreso);

        btnRegistrar.setOnClickListener(v -> registrar());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void registrar() {
        final String nombre = LoginActivity.texto(txtNombre);
        final String email = LoginActivity.texto(txtEmail);
        final String password = LoginActivity.texto(txtPassword);

        if (nombre.length() < 3) {
            txtNombre.setError("Minimo 3 caracteres");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Escribe un correo valido");
            return;
        }
        if (password.length() < 8) {
            txtPassword.setError("Minimo 8 caracteres");
            return;
        }
        if (!password.equals(LoginActivity.texto(txtConfirmar))) {
            txtConfirmar.setError("Las contrasenas no coinciden");
            return;
        }

        cargando(true);
        Tareas.ejecutar(() -> new ApiClient(null).crear(nombre, email, password, "MEMBER"),
                (usuario, error) -> {
                    if (isFinishing()) {
                        return;
                    }
                    cargando(false);
                    if (error != null) {
                        Snackbar.make(btnRegistrar, Mensajes.de(error), Snackbar.LENGTH_LONG).show();
                        return;
                    }
                    new AlertDialog.Builder(this)
                            .setTitle("Cuenta creada")
                            .setMessage("Te enviamos un correo de bienvenida. Tu cuenta queda en estado "
                                    + "PENDING hasta que un administrador la active; despues podras "
                                    + "iniciar sesion.")
                            .setPositiveButton("Entendido", (d, w) -> finish())
                            .setCancelable(false)
                            .show();
                });
    }

    private void cargando(boolean activo) {
        progreso.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!activo);
    }
}
