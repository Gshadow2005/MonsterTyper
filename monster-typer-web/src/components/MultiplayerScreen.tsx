import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import type { GameState, MPlayerView } from '../game/types';
import { SHOOTER_X } from '../game/constants';
import { sfx } from '../game/sound';
import { MonsterView } from './MonsterView';
import { ShooterView } from './ShooterView';
import { GlitchButton } from './GlitchButton';

function scrambleChar(shift: number, ch: string): string {
  const lower = ch.toLowerCase();
  if (!/^[a-z]$/.test(lower)) return ch;
  const mapped = String.fromCharCode(((lower.charCodeAt(0) - 97 + shift) % 26) + 97);
  return ch === ch.toUpperCase() && ch !== ch.toLowerCase() ? mapped.toUpperCase() : mapped;
}

export function MultiplayerScreen({
  snapshot,
  myId,
  role,
  winnerId,
  mode,
  onInput,
  onHostInput,
  onRematch,
  onLeave,
}: {
  snapshot: GameState;
  myId: string | null;
  role: 'host' | 'client';
  winnerId: string | null;
  mode: 'coop' | 'competitive';
  onInput: (text: string) => void;
  onHostInput: (text: string) => boolean;
  onRematch: () => void;
  onLeave: () => void;
}) {
  const arenaRef = useRef<HTMLDivElement>(null);
  const sizeRef = useRef({ w: 800, h: 700 });
  const angleRef = useRef(0);
  const [value, setValue] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const lastClearRef = useRef(0);

  const me: MPlayerView | null = snapshot.players.find((p) => p.id === myId) ?? null;
  const myClearSignal = me?.clearSignal ?? 0;

  useLayoutEffect(() => {
    const el = arenaRef.current;
    if (!el) return;
    const measure = () => {
      sizeRef.current = { w: el.clientWidth || 800, h: el.clientHeight || 700 };
    };
    measure();
    const ro = new ResizeObserver(measure);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  const { shooter, monsters } = snapshot;
  if (shooter.targetId !== null) {
    const target = monsters.find((m) => m.id === shooter.targetId);
    if (target) {
      const { w, h } = sizeRef.current;
      const dx = target.relX * w - SHOOTER_X * w;
      const dy = target.relY * h - h / 2;
      angleRef.current = (Math.atan2(dy, dx) * 180) / Math.PI;
    }
  }

  useEffect(() => {
    if (myClearSignal !== lastClearRef.current) {
      lastClearRef.current = myClearSignal;
      setValue('');
    }
  }, [myClearSignal]);

  useEffect(() => {
    if (snapshot.gameOver) setValue('');
  }, [snapshot.gameOver]);

  const prevExplosionsRef = useRef(0);
  const prevLaserRef = useRef(false);
  useEffect(() => {
    if (role !== 'client') return;
    if (snapshot.explosions.length > prevExplosionsRef.current) sfx.hurt.play();
    const laserOn = snapshot.shooter.laserTicksLeft > 0;
    if (laserOn && !prevLaserRef.current) sfx.gun.play();
    prevExplosionsRef.current = snapshot.explosions.length;
    prevLaserRef.current = laserOn;
  }, [snapshot, role]);

  const jammed = (me?.jamMsLeft ?? 0) > 0;
  const scrambled = (me?.scrambleMsLeft ?? 0) > 0;
  const eliminated = me?.eliminated ?? false;
  const inputDead = jammed || eliminated || snapshot.gameOver;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const v = e.target.value;
    const trimmed = v.trim().toLowerCase();
    const exactMatch = trimmed.length > 0 && snapshot.monsters.some((m) => m.word === trimmed);
    if (role === 'host') {
      if (onHostInput(v)) {
        setValue('');
        return;
      }
      setValue(v);
      return;
    }
    onInput(v);
    if (exactMatch) setValue('');
    else setValue(v);
  };

  const handleScrambledInput = (next: string) => {
    const trimmed = next.trim().toLowerCase();
    const exactMatch = trimmed.length > 0 && snapshot.monsters.some((m) => m.word === trimmed);
    if (role === 'host') {
      if (onHostInput(next)) setValue('');
      else setValue(next);
      return;
    }
    onInput(next);
    if (exactMatch) setValue('');
    else setValue(next);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && value === '') sfx.click.play();
    if (scrambled && me && /^[a-zA-Z]$/.test(e.key)) {
      e.preventDefault();
      const pos = e.currentTarget.selectionStart ?? value.length;
      const mapped = scrambleChar(me.scrambleShift, e.key);
      const next = value.slice(0, pos) + mapped + value.slice(pos);
      handleScrambledInput(next);
      requestAnimationFrame(() => {
        inputRef.current?.setSelectionRange(pos + 1, pos + 1);
      });
    }
  };

  const scale = Math.max(0.5, Math.min(sizeRef.current.w / 800, sizeRef.current.h / 700));

  const placeholder = eliminated
    ? 'ELIMINATED — spectating…'
    : snapshot.gameOver
      ? 'GAME OVER'
      : jammed
        ? `JAMMED — ${(me!.jamMsLeft / 1000).toFixed(1)}s`
        : scrambled
          ? 'SCRAMBLED — type the words anyway…'
          : 'type the monster words…';

  const sorted = [...snapshot.players].sort((a, b) => b.score - a.score);

  return (
    <div className="screen game-screen">
      <div className="hud mp-hud">
        <div className="hud-group">
          <div className="hud-label">{mode === 'coop' ? 'Team Score' : 'Difficulty'}</div>
          {mode === 'coop' ? (
            <div className="hud-score" key={`s-${snapshot.score}`}>{snapshot.score}</div>
          ) : (
            <div className={`difficulty-badge difficulty-${snapshot.difficulty}`}>{snapshot.difficulty.toUpperCase()}</div>
          )}
        </div>

        {mode === 'coop' && (
          <div className="hud-group">
            <div className="hud-label">Lives</div>
            <div className="hud-lives" key={`l-${snapshot.lives}`}>
              <span className="hearts">{snapshot.lives > 0 ? '♥'.repeat(Math.min(snapshot.lives, 10)) : '—'}</span>
            </div>
          </div>
        )}

        {mode === 'coop' && (
          <div className="hud-group">
            <div className="hud-label">Difficulty</div>
            <div className={`difficulty-badge difficulty-${snapshot.difficulty}`}>{snapshot.difficulty.toUpperCase()}</div>
          </div>
        )}

        <div className="roster-strip">
          {sorted.map((p) => (
            <div
              key={p.id}
              className={`roster-chip ${p.id === myId ? 'roster-me' : ''} ${p.eliminated ? 'roster-out' : ''}`}
              style={{ borderColor: p.color }}
            >
              <span className="roster-dot" style={{ backgroundColor: p.color }} />
              <span className="roster-name">{p.name}</span>
              <span className="roster-score" style={{ color: p.color }}>{p.score}</span>
              {mode === 'competitive' && (
                <span className="roster-lives">{p.eliminated ? 'OUT' : '♥'.repeat(Math.min(p.lives, 5))}</span>
              )}
            </div>
          ))}
        </div>

        <button className="back-button" onClick={onLeave}>
          Leave
        </button>
      </div>

      <div
        ref={arenaRef}
        className="arena mp-arena"
        onClick={() => document.querySelector<HTMLInputElement>('.mp-input')?.focus()}
      >
        <div className="arena-bg" style={{ backgroundImage: 'url(/assets/images/BGniKoKoAndMarie_2.png)' }} />

        <ShooterView
          angle={angleRef.current}
          laserActive={shooter.laserTicksLeft > 0}
          shake={shooter.shakeTicksLeft > 0}
          scale={scale}
          shotId={shooter.shotId}
        />

        {snapshot.flash && <div className="arena-flash" style={{ backgroundColor: snapshot.flash.color }} />}
        {snapshot.frozen && <div className="arena-freeze-tint" />}

        {snapshot.monsters.map((m) => (
          <MonsterView key={m.id} monster={m} scale={scale} typedText={value.trim().toLowerCase()} />
        ))}

        {snapshot.explosions.map((e) => (
          <img
            key={e.id}
            src="/assets/Explosion/Explosion.webp"
            alt=""
            className="explosion"
            style={{
              left: `${e.relX * 100}%`,
              top: `${e.relY * 100}%`,
              width: 210 * scale,
              height: 210 * scale,
            }}
            draggable={false}
          />
        ))}

        <div className="clouds" style={{ backgroundImage: 'url(/assets/images/CloudsniKoKoAndMarie_4.png)' }} />

        <div className="notifications">
          {snapshot.frozen && (
            <div className="notification freeze-chip">❄ FROZEN — {(snapshot.freezeMsLeft / 1000).toFixed(1)}s</div>
          )}
          {snapshot.notifications.map((n) => (
            <div key={n.id} className="notification" style={{ color: n.color }}>
              {n.text}
            </div>
          ))}
        </div>
      </div>

      <div className="input-bar">
        <div className="input-wrap">
          <span className={`status-chip jam ${jammed ? 'on' : ''}`}>
            <span className="chip-dot" />{jammed ? `JAM ${(me!.jamMsLeft / 1000).toFixed(1)}s` : 'JAM'}
          </span>
          <input
            ref={inputRef}
            className={`game-input mp-input ${jammed ? 'input-jammed' : scrambled ? 'input-scrambled' : ''} ${eliminated ? 'input-dead' : ''}`}
            type="text"
            value={value}
            readOnly={inputDead}
            spellCheck={false}
            autoComplete="off"
            placeholder={placeholder}
            onChange={handleChange}
            onKeyDown={handleKeyDown}
            autoFocus
          />
          <span className={`status-chip scramble ${scrambled ? 'on' : ''}`}>
            <span className="chip-dot" />{scrambled ? `SCRAMBLE ${(me!.scrambleMsLeft / 1000).toFixed(1)}s` : 'SCRAMBLE'}
          </span>
        </div>
      </div>

      {snapshot.gameOver && (
        <div className="modal-backdrop">
          <div className="modal gameover-modal">
            {mode === 'competitive' ? (
              <>
                <h2 className="winner-title">
                  {winnerId
                    ? `${snapshot.players.find((p) => p.id === winnerId)?.name ?? 'Someone'} wins!`
                    : 'No one survived!'}
                </h2>
                <div className="final-board">
                  {sorted.map((p, i) => (
                    <div key={p.id} className="final-row" style={{ borderColor: p.color }}>
                      <span>#{i + 1}</span>
                      <span className="roster-dot" style={{ backgroundColor: p.color }} />
                      <span className="final-name">{p.name}</span>
                      <span className="final-score">{p.score} pts</span>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <>
                <h2 className="winner-title gameover-red">TEAM WIPED OUT!</h2>
                <p className="gameover-score">Team Score: {snapshot.score}</p>
                <div className="final-board">
                  {sorted.map((p) => (
                    <div key={p.id} className="final-row" style={{ borderColor: p.color }}>
                      <span className="roster-dot" style={{ backgroundColor: p.color }} />
                      <span className="final-name">{p.name}</span>
                      <span className="final-score">{p.score} pts</span>
                    </div>
                  ))}
                </div>
              </>
            )}
            <div className="menu-buttons">
              {role === 'host' && <GlitchButton label="Rematch" onClick={onRematch} />}
              {role === 'client' && <p className="online-hint">Waiting for host to rematch…</p>}
              <GlitchButton label="Leave" onClick={onLeave} />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
