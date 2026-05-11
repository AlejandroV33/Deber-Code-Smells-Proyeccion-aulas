## Para correr el proyecto primero compilarlo con:
mvn clean compile
## Luego ejecutar desde el archivo main/java/app/Launcher
## Luego para sacar el reporte de SonarCube usar: 
docker run --rm \
  -e SONAR_HOST_URL="http://host.docker.internal:9000" \
  -e SONAR_SCANNER_OPTS="-Dsonar.projectKey=ProyeccionAulas" \
  -e SONAR_TOKEN="TU_TOKEN_REAL_AQUI" \
  -v "$(pwd):/usr/src" \
  sonarsource/sonar-scanner-cli
