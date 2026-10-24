package haiderpabon.guias.usuariosrender.vistas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import haiderpabon.guias.usuariosrender.R;
import haiderpabon.guias.usuariosrender.api.ApiClient;
import haiderpabon.guias.usuariosrender.api.Tareas;
import haiderpabon.guias.usuariosrender.datos.Sesion;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText txtEmail;
    private TextInputEditText txtPassword;
    private Button btnIngresar;
    private ProgressBar progreso;
    private TextView lblEstado;
    private Sesion sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sesion = new Sesion(this);
        if (sesion.activa()) {
            abrirListado();
            return;
        }
        setContentView(R.layout.activity_login);

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnIngresar = findViewById(R.id.btnIngresar);
        progreso = findViewById(R.id.progreso);
        lblEstado = findViewById(R.id.lblEstado);

        btnIngresar.setOnClickListener(v -> ingresar());
        findViewById(R.id.btnRegistrarse).setOnClickListener(
                v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void ingresar() {
        final String email = texto(txtEmail);
        final String password = texto(txtPassword);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Escribe un correo valido");
            return;
        }
        if (password.length() < 8) {
            txtPassword.setError("La contrasena tiene minimo 8 caracteres");
            return;
        }

        cargando(true);
        Tareas.ejecutar(() -> new ApiClient(null).login(email, password), (login, error) -> {
            if (isFinishing()) {
                return;
            }
            cargando(false);
            if (error != null) {
                Snackbar.make(btnIngresar, Mensajes.de(error), Snackbar.LENGTH_LONG).show();
                return;
            }
            sesion.guardar(login.token, login.expiraEn, email);
            abrirListado();
        });
    }

    private void abrirListado() {
        startActivity(new Intent(this, UsuariosActivity.class));
        finish();
    }

    private void cargando(boolean activo) {
        progreso.setVisibility(activo ? View.VISIBLE : View.GONE);
        lblEstado.setVisibility(activo ? View.VISIBLE : View.GONE);
        btnIngresar.setEnabled(!activo);
    }

    static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
