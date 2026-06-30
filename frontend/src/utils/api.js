/** Backend base URL from Vite env — same build works for dev and production. */
export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export async function createRoom() {
  const res = await fetch(`${API_URL}/api/rooms`, { method: 'POST' });
  if (!res.ok) throw new Error('Failed to create room');
  return res.json(); // { code }
}

export async function roomExists(code) {
  const res = await fetch(`${API_URL}/api/rooms/${encodeURIComponent(code)}`);
  return res.ok;
}
