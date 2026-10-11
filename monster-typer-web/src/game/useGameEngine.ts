import { useEffect, useRef, useState } from 'react';
import { GameEngine } from './engine';
import * as C from './constants';
import type { GameState } from './types';
import { gameMusic } from './sound';

export function useGameEngine() {
  const engineRef = useRef<GameEngine | null>(null);
  if (!engineRef.current) engineRef.current = new GameEngine();
  const engine = engineRef.current;

  const [snapshot, setSnapshot] = useState<GameState>(() => makeSnapshot(engine));
  const [best, setBest] = useState<number>(() => Number(localStorage.getItem(C.HIGH_SCORE_KEY) ?? 0));

  useEffect(() => {
    let raf = 0;
    let last = performance.now();
    let accumulator = 0;

    const loop = (time: number) => {
      raf = requestAnimationFrame(loop);
      accumulator += Math.min(time - last, 250);
      last = time;

      let steps = 0;
      while (accumulator >= C.TICK_MS && steps < 5) {
        engine.update();
        accumulator -= C.TICK_MS;
        steps += 1;
      }
      setSnapshot(makeSnapshot(engine));
    };
    raf = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(raf);
  }, [engine]);

  useEffect(() => {
    if (snapshot.gameOver) {
      gameMusic.stop();
      if (snapshot.score > best) {
        const newBest = snapshot.score;
        setBest(newBest);
        localStorage.setItem(C.HIGH_SCORE_KEY, String(newBest));
      }
    }
  }, [snapshot.gameOver, snapshot.score, best]);

  return { engine, snapshot, best };
}

export function makeSnapshot(engine: GameEngine): GameState {
  return {
    monsters: [...engine.monsters],
    explosions: [...engine.explosions],
    score: engine.score,
    lives: engine.lives,
    streak: engine.streak,
    difficulty: engine.difficulty,
    frozen: engine.frozen,
    jammed: engine.jammed,
    scrambled: engine.scrambled,
    freezeMsLeft: engine.freezeMsLeft,
    jamMsLeft: engine.jamMsLeft,
    scrambleMsLeft: engine.scrambleMsLeft,
    notifications: [...engine.notifications],
    flash: engine.flash,
    shooter: { ...engine.shooter },
    running: engine.running,
    paused: engine.paused,
    gameOver: engine.gameOver,
    typedText: engine.typedText,
    clearSignal: engine.clearSignal,
    mode: engine.mode,
    players: engine.getPlayersView(),
    winnerId: engine.winnerId,
  };
}
