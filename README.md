# loadofchat

Anonymous real-time chat — share a short room code, no sign-up required. Built with Spring Boot (STOMP/WebSocket) and React (Vite).

## Live URLs

| Service  | URL |
|----------|-----|
| Frontend | _Set after Render deploy — e.g. `https://loadofchat-frontend.onrender.com`_ |
| Backend  | _Set after Render deploy — e.g. `https://loadofchat-backend.onrender.com`_ |

> **Render free tier note:** The backend web service spins down after ~15 minutes of inactivity. The first request after idling can take **30–60 seconds** while the container cold-starts. This is normal Render behavior, not a bug in the app.

## How room codes work

1. **Create** — `POST /api/rooms` generates a random 6-character alphanumeric code (e.g. `K7M2XP`) and stores the room in memory.
2. **Share** — Copy the code from the chat room header and send it to someone else.
3. **Join** — Enter the code on the landing page; the frontend validates it exists (`GET /api/rooms/{code}`) before navigating to the room.
4. **Cleanup** — Rooms with zero participants for more than 10 minutes are removed by a scheduled task.

## WebSocket message flow

```
Browser                         Spring Boot
  │                                  │
  │──── SockJS connect /ws ─────────►│
  │◄─── STOMP CONNECTED ─────────────│
  │                                  │
  │── subscribe /topic/room/{code} ►│
  │                                  │
  │── send /app/chat/{code} ────────►│  (type: JOIN, sender: "Blue Otter")
  │                                  │── broadcast JOIN + PARTICIPANT_COUNT
  │◄── /topic/room/{code} ───────────│
  │                                  │
  │── send /app/chat/{code} ────────►│  (type: CHAT, content: "hello")
  │◄── /topic/room/{code} ───────────│  (all subscribers receive it)
  │                                  │
  │── send /app/chat/{code} ────────►│  (type: LEAVE)  — or disconnect
  │◄── LEAVE + PARTICIPANT_COUNT ────│
```

**Message payload shape:**

```json
{
  "type": "JOIN | CHAT | LEAVE | PARTICIPANT_COUNT",
  "sender": "Quiet Falcon",
  "content": "message text or system text",
  "timestamp": "2026-06-30T12:00:00Z",
  "participantCount": 2
}
```

`PARTICIPANT_COUNT` messages are server-generated and only include `type`, `participantCount`, and `timestamp`.

## Run locally

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+

### Backend (port 8080)

```bash
cd backend
mvn spring-boot:run
```

Environment variables (optional):

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | HTTP port |
| `ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated CORS/WebSocket origins |

### Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Create `frontend/.env` (or copy the included one):

```
VITE_API_URL=http://localhost:8080
```

Open [http://localhost:5173](http://localhost:5173).

### Local testing

1. Start backend and frontend.
2. Open two browser tabs at `http://localhost:5173`.
3. Tab A: **Create a Room** — note the code.
4. Tab B: **Join a Room** with that code.
5. Send messages in both tabs; confirm join/leave system messages and participant count update in real time.

Each tab gets its own anonymous nickname (stored in `sessionStorage`).

## Deploy to Render

The repo includes a [`render.yaml`](render.yaml) blueprint for one-step deployment.

### 1. Push to GitHub

```bash
git init   # if not already
git add .
git commit -m "Initial loadofchat implementation"
git remote add origin <your-github-repo-url>
git push -u origin main
```

### 2. New Blueprint on Render

1. Go to [Render Dashboard](https://dashboard.render.com) → **New** → **Blueprint**.
2. Connect the GitHub repo.
3. Render creates two services from `render.yaml`:
   - **loadofchat-backend** — Docker web service
   - **loadofchat-frontend** — static site

### 3. Set environment variables

After the backend deploys, note its URL (e.g. `https://loadofchat-backend.onrender.com`).

| Service | Variable | Example value |
|---------|----------|---------------|
| Backend | `ALLOWED_ORIGINS` | `https://loadofchat-frontend.onrender.com` |
| Frontend | `VITE_API_URL` | `https://loadofchat-backend.onrender.com` |

Redeploy the frontend after setting `VITE_API_URL` (Vite bakes it in at build time).

### 4. Verify across devices

1. Open the frontend URL on a laptop (Wi‑Fi).
2. Create a room and copy the code.
3. Open the same frontend URL on a phone (mobile data).
4. Join with the code.
5. Confirm messages, join/leave notifications, and participant count sync in real time.

## Project structure

```
loadofchat/
├── backend/                 # Spring Boot 3.2, Java 17
│   ├── Dockerfile
│   └── src/main/java/com/loadofchat/
│       ├── config/          # CORS, WebSocket, disconnect listener
│       ├── controller/      # REST: rooms + health
│       ├── service/         # RoomService + RoomStorage interface
│       ├── model/           # Room, ChatMessage
│       ├── dto/
│       └── websocket/       # STOMP message handler
├── frontend/                # React 18 + Vite
│   └── src/
│       ├── components/      # LandingPage, ChatRoom, MessageList, …
│       └── utils/           # API, WebSocket, nickname helpers
├── render.yaml
└── README.md
```

## Future work (out of scope)

- Persistent chat history (Redis / Postgres via `RoomStorage` swap)
- User authentication
- End-to-end message encryption
- Profanity filtering / moderation
- Horizontal scaling (Redis pub/sub or similar message broker for multi-instance WebSocket)

## License

MIT
