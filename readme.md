# Play demo

Acolyte Play demo for the interactive JDBC tour.

Online tour (legacy): [http://tour.acolyte.eu.org](http://tour.acolyte.eu.org)

## Requirements

- **SBT 1.9.9**
- **Play 3.0.12**
- **Scala 2.13.16**
- **Java 17** (local Docker/Koyeb runtime)

## Local development

```bash
sbt compile
sbt test
sbt run
```

Open [http://localhost:9000](http://localhost:9000).

## Docker

Build and run the production image locally (host port **8010**, container `PORT=8000`):

```bash
docker build -t acolyte-play-demo .
docker run --rm -p 8010:8000 -e PORT=8000 -e APPLICATION_SECRET="$(openssl rand -base64 48)" acolyte-play-demo
```

Health check:

```bash
curl -fsS http://localhost:8010/healthz
```

Expected response: `ok`

## Deploy on Koyeb

This app is packaged with a multi-stage **Dockerfile**. Koyeb builds from Git using the Docker builder.

### One-click Deploy to Koyeb button

[![Deploy to Koyeb](https://www.koyeb.com/static/images/deploy/button.svg)](https://app.koyeb.com/deploy?type=git&builder=docker&repository=github.com/cchantep/acolyte&branch=play-demo&name=acolyte-play-demo&service_type=web&instance_type=nano&regions=fra&ports=8000;http;/&env%5BPORT%5D=8000)

Button defaults:

| Setting | Value |
| --- | --- |
| Type | `git` |
| Builder | `docker` |
| Repository | `github.com/cchantep/acolyte` |
| Branch | `play-demo` |
| Service name | `acolyte-play-demo` |
| Service type | `web` |
| Instance | `nano` |
| Region | `fra` (Frankfurt) |
| Public port | `8000` HTTP `/` |
| Health check path | `/healthz` |
| Health check port | `8000` |

> The button deploys the GitHub `play-demo` branch. Push the local pending changes to `origin/play-demo` before using it.

### Required environment variable

| Name | Required | Notes |
| --- | --- | --- |
| `APPLICATION_SECRET` | **Yes** | Play production secret. Generate with `openssl rand -base64 48` and set it in the Koyeb service environment (or during the Deploy button flow). |
| `PORT` | No | Defaults to `8000` in the image and button URL. Koyeb may inject `PORT`; the container entrypoint binds `0.0.0.0:$PORT`. |

### Manual Koyeb settings (if not using the button)

1. Create a **Web Service** from GitHub `cchantep/acolyte`, branch `play-demo`.
2. Builder: **Dockerfile** (root `Dockerfile`).
3. Region: **fra**.
4. Ports: expose **8000** as HTTP.
5. Health checks: HTTP `GET /healthz` on port **8000**.
6. Set `APPLICATION_SECRET` (and optionally `PORT=8000`).

### Runtime notes

- The container starts Play with `-Dhttp.address=0.0.0.0 -Dhttp.port=${PORT:-8000}`.
- CSRF and AllowedHosts filters are disabled for this demo UI (static JS posts without tokens).
- No database is required; Acolyte is in-process.

## Heroku

Heroku-specific metadata (`system.properties`) was removed. Prefer Docker + Koyeb as above.
