import { useEffect, useRef } from 'react';

function formatTime(iso) {
  try {
    return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } catch {
    return '';
  }
}

export default function MessageList({ messages, currentUser }) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <ul className="message-list">
      {messages.map((msg, i) => {
        if (msg.type === 'PARTICIPANT_COUNT') return null;

        const isSystem = msg.type === 'JOIN' || msg.type === 'LEAVE';
        const isOwn = msg.type === 'CHAT' && msg.sender === currentUser;

        return (
          <li
            key={`${msg.timestamp}-${i}`}
            className={`message ${isSystem ? 'message-system' : ''} ${isOwn ? 'message-own' : ''}`}
          >
            {isSystem ? (
              <span className="message-system-text">{msg.content}</span>
            ) : (
              <>
                <div className="message-meta">
                  <span className="message-sender">{msg.sender}</span>
                  <span className="message-time">{formatTime(msg.timestamp)}</span>
                </div>
                <p className="message-body">{msg.content}</p>
              </>
            )}
          </li>
        );
      })}
      <li ref={bottomRef} aria-hidden="true" />
    </ul>
  );
}
