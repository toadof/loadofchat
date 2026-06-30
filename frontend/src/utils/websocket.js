import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_URL } from './api';

/**
 * Build the WebSocket URL from the REST API base URL.
 * http → ws, https → wss (required on Render TLS).
 */
function wsUrl() {
  const url = new URL(API_URL);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  url.pathname = '/ws';
  url.search = '';
  url.hash = '';
  return url.toString();
}

/**
 * Create and activate a STOMP client connected to the backend /ws endpoint.
 */
export function createStompClient(onConnect, onError) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_URL}/ws`),
    reconnectDelay: 3000,
    onConnect,
    onStompError: (frame) => onError?.(frame.headers['message'] || 'STOMP error'),
    onWebSocketError: (event) => onError?.(event.type || 'WebSocket error'),
  });

  return client;
}

export { wsUrl };
