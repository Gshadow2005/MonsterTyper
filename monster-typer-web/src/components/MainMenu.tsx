import { useEffect } from 'react';
import { GlitchButton } from './GlitchButton';
import { menuMusic } from '../game/sound';

export function MainMenu({ onPlay }: { onPlay: () => void }) {
  useEffect(() => {
    menuMusic.play();
  }, []);

  return (
    <div className="screen menu-screen" onClick={() => menuMusic.play()}>
      <img src="/assets/images/logo.gif" alt="logo" className="menu-logo" />
      <h1 className="menu-title">Monster Typer</h1>
      <div className="menu-buttons">
        <GlitchButton
          label="Play Game"
          onClick={() => {
            menuMusic.stop();
            onPlay();
          }}
        />
      </div>
    </div>
  );
}
