import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { roomExists } from '../utils/api';
import { getOrCreateNickname } from '../utils/nickname';
import { createStompClient } from '../utils/websocket';
import RoomHeader from './RoomHeader';
import MessageList from './MessageList';
import MessageInput from './MessageInput';

export default function ChatRoom() {
  const { code } = useParams();
  const navigate = useNavigate();
  const nickname = getOrCreateNickname();

  const [messages, setMessages] = useState([]);
  const [participantCount, setParticipantCount] = useState(0);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');
  const [copyFeedback, setCopyFeedback] = useState('');

  const clientRef = useRef(null);
  const joinedRef = useRef(false);
  const leavingRef = useRef(false);

  const addMessage = useCallback((msg) => {
    setMessages((prev) => [...prev, msg]);
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function init() {
      const exists = await roomExists(code);
      if (cancelled) return;
      if (!exists) {
        setError('This room no longer exists.');
        return;
      }

      const client = createStompClient(
        () => {
          if (cancelled) return;
          setConnected(true);
          setError('');

          client.subscribe(`/topic/room/${code}`, (frame) => {
            const msg = JSON.parse(frame.body);
            if (msg.type === 'PARTICIPANT_COUNT') {
              setParticipantCount(msg.participantCount ?? 0);
            } else {
              addMessage(msg);
            }
          });

          if (!joinedRef.current) {
            joinedRef.current = true;
            client.publish({
              destination: `/app/chat/${code}`,
              body: JSON.stringify({
                type: 'JOIN',
                sender: nickname,
                content: '',
                timestamp: new Date().toISOString(),
              }),
            });
          }
        },
        (err) => {
          if (!cancelled) setError(typeof err === 'string' ? err : 'Connection error');
        }
      );

      clientRef.current = client;
      client.activate();
    }

    init();

    return () => {
      cancelled = true;
      joinedRef.current = false;
      const client = clientRef.current;
      if (!leavingRef.current && client?.connected) {
        client.publish({
          destination: `/app/chat/${code}`,
          body: JSON.stringify({
            type: 'LEAVE',
            sender: nickname,
            content: '',
            timestamp: new Date().toISOString(),
          }),
        });
      }
      client?.deactivate();
      clientRef.current = null;
    };
  }, [code, nickname, addMessage]);

  function handleSend(content) {
    clientRef.current?.publish({
      destination: `/app/chat/${code}`,
      body: JSON.stringify({
        type: 'CHAT',
        sender: nickname,
        content,
        timestamp: new Date().toISOString(),
      }),
    });
  }

  function handleLeave() {
    leavingRef.current = true;
    const client = clientRef.current;
    if (client?.connected) {
      client.publish({
        destination: `/app/chat/${code}`,
        body: JSON.stringify({
          type: 'LEAVE',
          sender: nickname,
          content: '',
          timestamp: new Date().toISOString(),
        }),
      });
    }
    client?.deactivate();
    navigate('/');
  }

  async function handleCopy() {
    try {
      await navigator.clipboard.writeText(code);
      setCopyFeedback('Copied!');
      setTimeout(() => setCopyFeedback(''), 2000);
    } catch {
      setCopyFeedback('Copy failed');
    }
  }

  if (error && !connected) {
    return (
      <main className="room room-error">
        <p>{error}</p>
        <button type="button" className="btn btn-secondary" onClick={() => navigate('/')}>
          Back to home
        </button>
      </main>
    );
  }

  return (
    <main className="room">
      <RoomHeader
        code={code}
        participantCount={participantCount}
        nickname={nickname}
        onCopy={handleCopy}
        onLeave={handleLeave}
      />
      {copyFeedback && <p className="copy-feedback">{copyFeedback}</p>}
      {!connected && <p className="connecting">Connecting…</p>}
      <MessageList messages={messages} currentUser={nickname} />
      <MessageInput onSend={handleSend} disabled={!connected} />
    </main>
  );
}
