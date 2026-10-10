# Back_esiBuy

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