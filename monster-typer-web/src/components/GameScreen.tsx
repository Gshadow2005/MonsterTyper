import { useEffect, useLayoutEffect, useRef } from 'react';
import type { GameEngine } from '../game/engine';
import type { GameState } from '../game/types';
import { SHOOTER_X } from '../game/constants';
import { gameMusic } from '../game/sound';
import { Hud } from './Hud';
import { InputBar } from './InputBar';
import { MonsterView } from './MonsterView';
import { ShooterView } from './ShooterView';
import { GameOverScreen } from './GameOverScreen';

export function GameScreen({
  engine,
  snapshot,
  best,
  onBackToMenu,
}: {
  engine: GameEngine;
  snapshot: GameState;
  best: number;
  onBackToMenu: () => void;
}) {
  const arenaRef = useRef<HTMLDivElement>(null);
  const sizeRef = useRef({ w: 800, h: 700 });
  const angleRef = useRef(0);

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
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') engine.togglePause();
    };
    const onBlur = () => {
      if (engine.running && !engine.paused) engine.paused = true;
    };
    window.addEventListener('keydown', onKey);
    window.addEventListener('blur', onBlur);
    return () => {
      window.removeEventListener('keydown', onKey);
      window.removeEventListener('blur', onBlur);
    };
  }, [engine]);

  const scale = Math.max(0.5, Math.min(sizeRef.current.w / 800, sizeRef.current.h / 700));

  return (
    <div className="screen game-screen">
      <Hud
        score={snapshot.score}
        lives={snapshot.lives}
        streak={snapshot.streak}
        best={best}
        difficulty={snapshot.difficulty}
        onBackToMenu={onBackToMenu}
      />

      <div ref={arenaRef} className="arena" onClick={() => document.querySelector<HTMLInputElement>('.game-input')?.focus()}>
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
          <MonsterView key={m.id} monster={m} scale={scale} typedText={snapshot.typedText} />
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

        {snapshot.paused && !snapshot.gameOver && (
          <div className="modal-backdrop pause-overlay">
            <h2 className="pause-title">PAUSED</h2>
            <p>Press Esc to resume</p>
          </div>
        )}
      </div>

      <InputBar
        engine={engine}
        snapshotClearSignal={snapshot.clearSignal}
        jammed={snapshot.jammed}
        scrambled={snapshot.scrambled}
        jamMsLeft={snapshot.jamMsLeft}
        scrambleMsLeft={snapshot.scrambleMsLeft}
      />

      {snapshot.gameOver && (
        <GameOverScreen
          score={snapshot.score}
          best={best}
          onPlayAgain={() => {
            engine.start();
            gameMusic.play();
          }}
          onMainMenu={onBackToMenu}
        />
      )}
    </div>
  );
}
