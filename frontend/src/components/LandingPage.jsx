import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createRoom, roomExists } from '../utils/api';

export default function LandingPage() {
  const navigate = useNavigate();
  const [joinCode, setJoinCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleCreate() {
    setError('');
    setLoading(true);
    try {
      const { code } = await createRoom();
      navigate(`/room/${code}`);
    } catch {
      setError('Could not create a room. Is the backend running?');
    } finally {
      setLoading(false);
    }
  }

  async function handleJoin(e) {
    e.preventDefault();
    const code = joinCode.trim().toUpperCase();
    if (!code) {
      setError('Enter a room code.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const exists = await roomExists(code);
      if (!exists) {
        setError('Room not found. Check the code and try again.');
        return;
      }
      navigate(`/room/${code}`);
    } catch {
      setError('Could not reach the server. Is the backend running?');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="landing">
      <div className="landing-card">
        <h1 className="landing-title">loadofchat</h1>
        <p className="landing-subtitle">
          Anonymous real-time chat. No sign-up — just share a room code.
        </p>

        <button
          type="button"
          className="btn btn-primary btn-block"
          onClick={handleCreate}
          disabled={loading}
        >
          Create a Room
        </button>

        <div className="landing-divider">
          <span>or</span>
        </div>

        <form onSubmit={handleJoin} className="join-form">
          <input
            type="text"
            className="input"
            placeholder="Enter room code"
            value={joinCode}
            onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
            maxLength={6}
            autoComplete="off"
            spellCheck={false}
          />
          <button type="submit" className="btn btn-secondary btn-block" disabled={loading}>
            Join a Room
          </button>
        </form>

        {error && <p className="error-text">{error}</p>}
      </div>
    </main>
  );
}
