# Joke API — Java 25 / Spring Boot

A joke management service with a web UI, SQLite storage, and Ukrainian TTS audio.  
Converted from Python/FastAPI to **Java 25 + Spring Boot 3.5**.

---

## Requirements

| Tool | Version |
|------|---------|
| JDK  | 25      |
| Maven | 3.9+   |
| Docker | any (for container run) |

---

## Run locally

```bash
cd joke-api-java

# build
mvn package -DskipTests

# run
java -jar target/joke-api-1.0.0.jar
```

Open **http://localhost:8091** in your browser.

### Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_PATH` | `./data/jokes.db` | SQLite database file path |
| `AUDIO_DIR` | `/data/audio` | Directory where TTS mp3 files are written |
| `PORT` | `8091` | HTTP server port |

Override on the command line:

```bash
DB_PATH=./jokes.db AUDIO_DIR=./audio java -jar target/joke-api-1.0.0.jar
```

---

## Run with Docker

```bash
cd joke-api-java

# build image
docker build -t joke-api-java .

# run (persists DB and audio between restarts)
docker run -p 8091:8091 \
  -v $(pwd)/data:/data \
  -e DB_PATH=/data/jokes.db \
  -e AUDIO_DIR=/data/audio \
  joke-api-java
```

---

## API reference

| Method | Path | Body / Params | Description |
|--------|------|---------------|-------------|
| `GET` | `/api/joke` | — | Random joke `{"joke":"..."}` |
| `GET` | `/api/joke/audio` | — | Generate TTS mp3, returns `{"url":"/api/audio/<id>.mp3"}` |
| `GET` | `/api/audio/{filename}` | — | Stream the mp3 file |
| `GET` | `/api/all` | — | All jokes as JSON array |
| `POST` | `/api/joke` | form: `text=...` | Add a single joke |
| `DELETE` | `/api/joke/{id}` | — | Delete joke by ID |
| `DELETE` | `/api/clear` | — | Delete all jokes |
| `POST` | `/api/import` | multipart: `file=<txt>` | Bulk import (one joke per line) |

---

## TTS audio

`GET /api/joke/audio` fetches a random joke, synthesises it in **Ukrainian** via the Google Translate TTS endpoint (the same mechanism used by the original Python `gTTS` library), saves the mp3 to `AUDIO_DIR`, and returns a URL you can play in the browser.

> **Note:** The Google Translate TTS endpoint is an unofficial, rate-limited API. For production use, replace `JokeService.generateAudio()` with the [Google Cloud Text-to-Speech API](https://cloud.google.com/text-to-speech).

---

## Database

- SQLite file, auto-created on first run at `DB_PATH`.
- Schema is managed by Hibernate (`ddl-auto=update`).
- On first startup, if the DB is empty, jokes are seeded from `src/main/resources/jokes.txt` (one joke per line).

---

## Project structure

```
joke-api-java/
├── Dockerfile
├── pom.xml
└── src/main/
    ├── java/com/jokeapi/
    │   ├── JokeApiApplication.java      # entry point
    │   ├── controller/JokeController.java
    │   ├── model/Joke.java
    │   ├── repository/JokeRepository.java
    │   └── service/JokeService.java     # business logic + TTS + seeding
    └── resources/
        ├── application.properties
        ├── jokes.txt                    # seed data
        └── static/                      # frontend (served at /)
            ├── index.html
            ├── app.js
            └── style.css
```


docker build -t java-joke-api . 