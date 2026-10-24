# Usuarios Render (Android - Java)

App Android que consume la API REST de gestión de usuarios desplegada en Render
(proyecto de las guías 1 a 5: Spring Boot + TiDB + Render + Brevo).

- API: https://users-management-render-haider-pabon.onrender.com
- Swagger: https://users-management-render-haider-pabon.onrender.com/swagger-ui/index.html

## Qué hace

| Pantalla | Endpoint | Quién |
|---|---|---|
| Iniciar sesión | `POST /api/auth/login` | Cualquiera con cuenta ACTIVE |
| Crear cuenta | `POST /api/users` (rol MEMBER) | Público |
| Listado de usuarios | `GET /api/users` | ADMIN y REVIEWER |
| Nuevo usuario | `POST /api/users` (+ `PUT` si se elige un estado distinto de PENDING) | ADMIN |
| Editar usuario | `PUT /api/users/{id}` | ADMIN |
| Eliminar usuario | `DELETE /api/users/{id}` | ADMIN |

- El token JWT se guarda en `SharedPreferences` y se envía en el encabezado
  `Authorization: Bearer <token>`. El rol y el id se leen del propio token.
- Cuando el token vence (15 min) la app vuelve al login.
- Las peticiones se hacen en un hilo aparte con `ExecutorService` y `HttpURLConnection`,
  y las respuestas JSON se leen con `org.json` (incluido en Android).
- Render (plan gratis) duerme la app; la primera petición puede tardar cerca de un minuto.

## Estructura

```
app/src/main/java/haiderpabon/guias/usuariosrender/
├── api/      ApiClient (peticiones HTTP), ApiException, Tareas (hilos)
├── datos/    Usuario, Sesion (token JWT)
└── vistas/   LoginActivity, RegistroActivity, UsuariosActivity, UsuarioAdapter, UsuarioFormActivity
```

## Cómo ejecutarla

1. Abrir esta carpeta en Android Studio (File → Open).
2. Esperar a que termine el Gradle Sync.
3. Ejecutar en el emulador o en un celular con internet.
4. Iniciar sesión con el administrador de la API.

## APK

El APK listo para instalar está en la sección **Releases** del repositorio.
