# Back_esiBuy

## Arranque local

La aplicación lee su configuración local desde un fichero `.env` en la raíz del
repositorio. Crea uno a partir de la plantilla:

```powershell
Copy-Item .env.example .env
```

Antes de arrancar, sustituye los valores de `jwt.secret`, `MONGODB_URI`,
`mail.username`, `mail.password` y `mfa.encryption.key`. Genera el secreto JWT
con 32 bytes aleatorios (se representa en hexadecimal):

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToHexString($bytes)
```

Copia el resultado en `jwt.secret`. Para `mfa.encryption.key`, usa una clave
aleatoria de exactamente 16, 24 o 32 caracteres ASCII. El perfil local `dev`
mantiene una clave heredada para no romper los datos de desarrollo existentes;
no uses esa clave para una instalación nueva de producción. Si ya hay secretos
TOTP cifrados en MongoDB, conserva la clave usada para cifrarlos: cambiarla
impedirá descifrarlos y requerirá que esos usuarios vuelvan a configurar MFA.
Puedes generar una clave de 32 caracteres con PowerShell:

```powershell
$bytes = [byte[]]::new(24)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes).Replace('+', '-').Replace('/', '_')
```

Necesitas MongoDB disponible en la dirección configurada en `MONGODB_URI`; la
configuración de correo debe ser válida para enviar correos de recuperación. El
fichero `.env` está excluido de Git para no publicar credenciales.

## Despliegue en Render

En Render, crea un Web Service desde el Blueprint `render.yaml`; usa el nombre
`esibuy-api` si está disponible. Render asignará entonces una URL del tipo
`https://esibuy-api.onrender.com`; configura el frontend con la URL exacta que
Render confirme. El Blueprint activa el perfil `prod` y usa el `Dockerfile`.
Configura los valores privados solicitados por Render:

- `MONGODB_URI`: URI de MongoDB Atlas.
- `JWT_SECRET`: secreto aleatorio de al menos 32 bytes.
- `JWT_EXPIRATION` y `JWT_REFRESH_EXPIRATION`: duración en milisegundos.
- `MAIL_USERNAME` y `MAIL_PASSWORD`: credenciales SMTP.
- `MFA_ENCRYPTION_KEY`: clave de 16, 24 o 32 caracteres ASCII. Debe ser estable
  entre despliegues y respaldarse de forma segura.
- `CORS_ALLOWED_ORIGINS=https://esibuy.web.app` (sin barra final).
- `COOKIE_SAME_SITE=None` mientras frontend y API usen dominios de Firebase y
  Render; si se configuran dominios propios bajo el mismo dominio registrable,
  cambia a `Lax`.
- `PASSWORD_RESET_URL=https://esibuy.web.app/reset-password?token=`.

Render asigna el dominio público de la API al crear el servicio; no es necesario
configurar ese dominio en CORS. Configura el frontend para llamar a ese dominio
HTTPS y enviar credenciales (`credentials: "include"`). En cada sesión nueva,
el frontend debe solicitar `GET /csrf`, conservar `token` y enviar su valor en
la cabecera indicada por `headerName` en las peticiones `POST`, `PUT`, `PATCH` y
`DELETE`. El token JWT se entrega solo en cookies `HttpOnly`, no en el JSON.

**Importante:** `esibuy.web.app` y el dominio `onrender.com` son sitios distintos.
Aunque las cookies de producción se configuran como `SameSite=None; Secure`, los
navegadores pueden bloquear cookies de terceros. Para una sesión fiable en
producción, configura dominios propios bajo el mismo dominio registrable (por
ejemplo, `www.midominio.com` y `api.midominio.com`) y ajusta `CORS_ALLOWED_ORIGINS`,
`PASSWORD_RESET_URL` y `COOKIE_SAME_SITE=Lax`; o sirve la API tras un proxy de
mismo origen.

### CI/CD con GitHub Actions

El workflow `.github/workflows/maven.yml` ejecuta `mvn verify` y construye la
imagen Docker en cada pull request o push a `main`/`develop`. Solo un push a
`main` que supere ambos pasos activa el despliegue en Render. También se puede
lanzar manualmente desde **Actions > CI/CD API > Run workflow**, seleccionando
`main`.

Para habilitar el despliegue automático:

1. Crea en Render el Blueprint de `render.yaml`, completa las variables
   requeridas y espera a que el servicio esté disponible por HTTPS.
2. En Render, abre el servicio de la API, ve a **Settings > Deploy Hook** y
   copia la URL del deploy hook. Trátala como una contraseña; no la publiques.
3. En GitHub, abre **Settings > Environments**, crea el entorno `production` y
   añade `RENDER_DEPLOY_HOOK_URL` como secreto del entorno con esa URL.
4. En **Settings > Branches**, protege `main` y exige que termine correctamente
   el job `Test and build Docker image` antes de permitir el merge.
5. Abre un pull request hacia `main`, verifica que el workflow pase y haz merge.
   El job de despliegue llamará al hook de Render. Comprueba el resultado y los
   logs de arranque en el panel de Render.

`autoDeploy: false` evita que Render despliegue por su cuenta al detectar un
cambio del repositorio: el workflow es el único que inicia el despliegue y le
indica a Render el SHA exacto que ya pasó CI. El workflow no necesita acceso a
las credenciales de MongoDB, JWT, correo ni MFA; esas variables permanecen
guardadas en Render.

## Categorías

Los administradores pueden crear categorías mediante `POST /admin/categorias`.
Los nombres duplicados se responden con HTTP 409.

## Cobertura con JaCoCo

La cobertura se genera durante la fase `verify`, junto con la ejecución de los
tests:

```powershell
.\mvnw.cmd clean verify
```

Los informes se encuentran en `target\site\jacoco\`:

- `jacoco.xml`: informe para SonarQube.
- `jacoco.csv`: informe tabular.
- `index.html`: informe navegable en el navegador.

El fichero binario de ejecución se genera en `target\jacoco.exec`. SonarQube
está configurado para leer `target/site/jacoco/jacoco.xml`; por tanto, primero
hay que ejecutar `verify` y después lanzar el análisis de SonarQube.