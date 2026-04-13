import os
import sqlite3
from fastapi import FastAPI, UploadFile, File, Form
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
from fastapi.middleware.httpsredirect import HTTPSRedirectMiddleware


# =========================
# CONFIG
# =========================

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

DB_PATH = os.getenv(
    "DB_PATH",
    os.path.join(BASE_DIR, "data", "jokes.db")
)

os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
JOKES_FILE = "./jokes.txt"

app = FastAPI()

# =========================
# STATIC UI
# =========================

app.mount("/static", StaticFiles(directory="static"), name="static")


@app.get("/")
def root():
    return FileResponse("static/index.html")


# =========================
# DB HELPERS
# =========================

def get_conn():
    return sqlite3.connect(DB_PATH)


def init_db():
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("""
        CREATE TABLE IF NOT EXISTS jokes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            text TEXT NOT NULL
        )
    """)
    conn.commit()
    conn.close()


def seed_db_if_empty():
    if not os.path.exists(JOKES_FILE):
        return

    conn = get_conn()
    cur = conn.cursor()

    cur.execute("SELECT COUNT(*) FROM jokes")
    count = cur.fetchone()[0]

    if count == 0:
        print("Seeding jokes from jokes.txt...")
        with open(JOKES_FILE, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line:
                    cur.execute("INSERT INTO jokes(text) VALUES (?)", (line,))

        conn.commit()

    conn.close()


# =========================
# FASTAPI STARTUP
# =========================

@app.on_event("startup")
def startup():
    init_db()
    seed_db_if_empty()


# =========================
# API
# =========================

@app.get("/api/joke")
def get_joke():
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("SELECT text FROM jokes ORDER BY RANDOM() LIMIT 1")
    row = cur.fetchone()
    conn.close()

    return {"joke": row[0] if row else "No jokes found"}


@app.get("/api/all")
def get_all():
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("SELECT id, text FROM jokes")
    rows = cur.fetchall()
    conn.close()

    return [{"id": r[0], "text": r[1]} for r in rows]


@app.post("/api/joke")
def add_joke(text: str = Form(...)):
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("INSERT INTO jokes(text) VALUES (?)", (text,))
    conn.commit()
    conn.close()

    return {"status": "ok"}


@app.delete("/api/joke/{joke_id}")
def delete_joke(joke_id: int):
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("DELETE FROM jokes WHERE id = ?", (joke_id,))
    conn.commit()
    conn.close()

    return {"status": "deleted"}


@app.delete("/api/clear")
def clear_jokes():
    conn = get_conn()
    cur = conn.cursor()
    cur.execute("DELETE FROM jokes")
    conn.commit()
    conn.close()

    return {"status": "cleared"}


@app.post("/api/import")
async def import_jokes(file: UploadFile = File(...)):
    content = (await file.read()).decode("utf-8").splitlines()

    conn = get_conn()
    cur = conn.cursor()

    for line in content:
        line = line.strip()
        if line:
            cur.execute("INSERT INTO jokes(text) VALUES (?)", (line,))

    conn.commit()
    conn.close()

    return {"status": "imported"}