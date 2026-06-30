export default function RoomHeader({ code, participantCount, nickname, onCopy, onLeave }) {
  return (
    <header className="room-header">
      <div className="room-header-left">
        <span className="room-label">Room</span>
        <div className="room-code-row">
          <code className="room-code">{code}</code>
          <button type="button" className="btn btn-ghost btn-sm" onClick={onCopy} title="Copy code">
            Copy
          </button>
        </div>
      </div>
      <div className="room-header-center">
        <span className="participant-badge">{participantCount} online</span>
        <span className="nickname-badge">You: {nickname}</span>
      </div>
      <button type="button" className="btn btn-danger btn-sm" onClick={onLeave}>
        Leave Room
      </button>
    </header>
  );
}
