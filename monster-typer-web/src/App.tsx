import { useEffect, useState } from 'react';
import { useGameEngine } from './game/useGameEngine';
import { gameMusic, menuMusic } from './game/sound';
import { MainMenu } from './components/MainMenu';
import { GameScreen } from './components/GameScreen';
import { OnlineScreen } from './components/OnlineScreen';
import { MultiplayerScreen } from './components/MultiplayerScreen';
import { useMultiplayer } from './net/useMultiplayer';

type Screen = 'menu' | 'game' | 'online';

const SMALL_SCREEN_QUERY = '(max-width: 820px), (max-height: 560px)';

function useSmallScreen(): boolean {
  const [small, setSmall] = useState(() => window.matchMedia(SMALL_SCREEN_QUERY).matches);
  useEffect(() => {
    const mq = window.matchMedia(SMALL_SCREEN_QUERY);
    const onChange = (e: MediaQueryListEvent) => setSmall(e.matches);
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, []);
  return small;
}

export function App() {
  const { engine, snapshot, best } = useGameEngine();
  const mp = useMultiplayer();
  const [screen, setScreen] = useState<Screen>('menu');
  const smallScreen = useSmallScreen();

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

  const openOnline = () => {
    menuMusic.stop();
    setScreen('online');
  };

  const closeOnline = () => {
    mp.teardown();
    menuMusic.play();
    setScreen('menu');
  };

  if (smallScreen) {
    return (
      <div className="screen menu-screen small-screen-block">
        <h1 className="online-title">Monster Typer</h1>
        <p className="small-screen-message">Please play this game on a bigger screen.</p>
      </div>
    );
  }

  if (screen === 'game') {
    return <GameScreen engine={engine} snapshot={snapshot} best={best} onBackToMenu={backToMenu} />;
  }

  if (screen === 'online') {
    if ((mp.phase === 'playing' || mp.phase === 'ended') && mp.snapshot) {
      return (
        <MultiplayerScreen
          snapshot={mp.snapshot}
          myId={mp.myId}
          role={mp.role ?? 'client'}
          winnerId={mp.winnerId}
          mode={mp.mode}
          hostStalled={mp.hostStalled}
          onInput={mp.sendInput}
          onHostInput={mp.submitHostInput}
          onRematch={mp.rematch}
          onChangeMode={mp.returnToLobby}
          onLeave={closeOnline}
        />
      );
    }
    if (mp.phase === 'playing') {
      return (
        <div className="screen menu-screen">
          <h1 className="online-title">Starting…</h1>
        </div>
      );
    }
    return (
      <OnlineScreen
        phase={mp.phase}
        code={mp.code}
        players={mp.players}
        mode={mp.mode}
        role={mp.role}
        errorMsg={mp.errorMsg}
        myId={mp.myId}
        onCreate={mp.createRoom}
        onJoin={mp.joinRoom}
        onStart={mp.startGame}
        onModeChange={mp.changeMode}
        onLeave={closeOnline}
      />
    );
  }

  return <MainMenu onPlay={startGame} onOnline={openOnline} />;
}
