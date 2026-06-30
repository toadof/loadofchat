import { Routes, Route } from 'react-router-dom';
import LandingPage from './components/LandingPage';
import ChatRoom from './components/ChatRoom';
import './App.css';
import './chat.css';

export default function App() {
  return (
    <div className="app">
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/room/:code" element={<ChatRoom />} />
      </Routes>
    </div>
  );
}
