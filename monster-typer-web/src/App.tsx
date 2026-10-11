import { useState } from 'react';
import { useGameEngine } from './game/useGameEngine';
import { gameMusic, menuMusic } from './game/sound';
import { MainMenu } from './components/MainMenu';
import { GameScreen } from './components/GameScreen';

type Screen = 'menu' | 'game';

export function App() {
  const { engine, snapshot, best } = useGameEngine();
  const [screen, setScreen] = useState<Screen>('menu');

  const startGame = () => {
    engine.start();
    menuMusic.stop();
    gameMusic.play();
    setScreen('game');
  };

  const backToMenu = () => {
    engine.reset();
    gameMusic.stop();
    menuMusic.play();
    setScreen('menu');
  };

  return screen === 'menu' ? (
    <MainMenu onPlay={startGame} />
  ) : (
    <GameScreen engine={engine} snapshot={snapshot} best={best} onBackToMenu={backToMenu} />
  );
}
