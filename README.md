# Back_esiBuy

## Arranque local

La aplicación lee su configuración desde un fichero `.env` en la raíz del
repositorio. Crea uno a partir de la plantilla:

```powershell
Copy-Item .env.example .env
```

Antes de arrancar, sustituye los valores de `jwt.secret`, `mail.username` y
`mail.password`. Para generar una clave JWT aleatoria de 32 bytes en PowerShell:

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToHexString($bytes)
```

Copia el resultado en `jwt.secret`. Necesitas MongoDB disponible en la dirección
configurada en `MONGODB_URI`; la configuración de correo debe ser válida para
enviar correos de recuperación. El fichero `.env` está excluido de Git para no
publicar credenciales.

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