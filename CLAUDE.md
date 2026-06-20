# docker-spring-project

Spring Boot 3.4.9 REST API that bulk-imports a CSV of baseball players into MySQL using multi-threaded batch processing. Runs as a 3-container Docker stack (app, MySQL, Redis).

## Build

```bash
./mvnw clean package          # build JAR (target/docker-spring.jar)
./mvnw test                   # run unit tests only
```

## Running containers

```bash
./bring-up.sh                 # build image + docker-compose up -d
./bring-down.sh               # docker-compose down + remove image
```

All three containers must be running before hitting any API endpoint.

## API

```
POST http://localhost:8080/api/person/import?filePath=/home/app/People.csv
```

`People.csv` is baked into the Docker image at `/home/app/People.csv`.

## CI/CD

GitHub Actions workflow at `.github/workflows/ci-cd.yml`:
- **test job** — runs on `ubuntu-latest` (GitHub-hosted, free). Sets up Java 17 and runs `./mvnw test`.
- **deploy job** — runs on `self-hosted` (this Mac). Builds JAR + Docker image, restarts containers, polls `/actuator/health` until `"status":"UP"`.

## Docker

- App image: `docker-spring` (built locally from `Dockerfile`)
- MySQL healthcheck gates app startup — app will not start until MySQL is ready
- Redis is wired in as a dependency in docker compose but not yet used in application logic
