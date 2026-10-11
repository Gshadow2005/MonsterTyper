import { useState } from 'react';
import { GlitchButton } from './GlitchButton';
import { sfx } from '../game/sound';
import type { GameMode, LobbyPlayer } from '../game/types';
import type { MpPhase } from '../net/useMultiplayer';

export function OnlineScreen({
  phase,
  code,
  players,
  mode,
  role,
  errorMsg,
  myId,
  onCreate,
  onJoin,
  onStart,
  onModeChange,
  onLeave,
}: {
  phase: MpPhase;
  code: string;
  players: LobbyPlayer[];
  mode: GameMode;
  role: 'host' | 'client' | null;
  errorMsg: string;
  myId: string | null;
  onCreate: (name: string) => void;
  onJoin: (code: string, name: string) => void;
  onStart: () => void;
  onModeChange: (mode: GameMode) => void;
  onLeave: () => void;
}) {
  const [name, setName] = useState('');
  const [joinCode, setJoinCode] = useState('');

  const startClick = () => {
    sfx.click.play();
    if (name.trim()) onCreate(name.trim());
  };

  const joinClick = () => {
    sfx.click.play();
    if (joinCode.trim()) onJoin(joinCode.trim(), name.trim());
  };

  if (phase === 'connecting') {
    return (
      <div className="screen menu-screen">
        <h1 className="online-title">Connecting…</h1>
        <p className="online-hint">Room {code}</p>
        <GlitchButton label="Cancel" onClick={onLeave} />
      </div>
    );
  }

  if (phase === 'lobby') {
    return (
      <div className="screen menu-screen">
        <h1 className="online-title">Lobby</h1>
        <div className="room-code-box">
          <span className="room-code-label">Room Code</span>
          <span className="room-code">{code}</span>
          <span className="room-code-hint">Friends: enter this code to join</span>
        </div>

        <div className="mode-toggle">
          <button
            className={`mode-btn ${mode === 'coop' ? 'mode-btn-active' : ''}`}
            onClick={() => role === 'host' && onModeChange('coop')}
            disabled={role !== 'host'}
          >
            Co-op
          </button>
          <button
            className={`mode-btn ${mode === 'competitive' ? 'mode-btn-active' : ''}`}
            onClick={() => role === 'host' && onModeChange('competitive')}
            disabled={role !== 'host'}
          >
            Last Standing
          </button>
        </div>
        <p className="online-hint">
          {mode === 'coop' ? 'Shared lives and score — survive together!' : 'Individual lives — last one standing wins!'}
        </p>

        <div className="lobby-players">
          {players.map((p) => (
            <div key={p.id} className="lobby-player" style={{ borderColor: p.color }}>
              <span className="lobby-dot" style={{ backgroundColor: p.color }} />
              {p.name}
              {p.id === myId ? ' (you)' : ''}
              {role === 'host' && p.id === myId ? ' 👑' : ''}
            </div>
          ))}
        </div>

        {role === 'host' ? (
          <GlitchButton label="Start Game" onClick={() => { sfx.click.play(); onStart(); }} />
        ) : (
          <p className="online-hint">Waiting for the host to start…</p>
        )}
        <GlitchButton label="Leave" onClick={onLeave} />
      </div>
    );
  }

  if (phase === 'error') {
    return (
      <div className="screen menu-screen">
        <h1 className="online-title online-error">{errorMsg}</h1>
        <GlitchButton label="Back" onClick={onLeave} />
      </div>
    );
  }

  return (
    <div className="screen menu-screen">
      <h1 className="online-title">Play Online</h1>
      <div className="online-form">
        <input
          className="online-input"
          placeholder="Your name"
          maxLength={12}
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <GlitchButton label="Create Room" onClick={startClick} />
        <div className="online-divider">or join a room</div>
        <input
          className="online-input online-code-input"
          placeholder="ROOM CODE"
          maxLength={4}
          value={joinCode}
          onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
        />
        <GlitchButton label="Join Room" onClick={joinClick} />
      </div>
      <GlitchButton label="Back" onClick={onLeave} />
    </div>
  );
}
