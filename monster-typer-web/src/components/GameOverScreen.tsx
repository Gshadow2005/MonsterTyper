import { useEffect } from 'react';
import { GlitchButton } from './GlitchButton';
import { sfx } from '../game/sound';

export function GameOverScreen({
  score,
  best,
  onPlayAgain,
  onMainMenu,
}: {
  score: number;
  best: number;
  onPlayAgain: () => void;
  onMainMenu: () => void;
}) {
  useEffect(() => {
    sfx.gameOver.play();
  }, []);

  return (
    <div className="modal-backdrop">
      <div className="modal gameover-modal">
        <img src="/assets/images/GameOver.gif" alt="Game Over" className="gameover-logo" />
        <p className="gameover-score">Your Score: {score}</p>
        {score >= best && score > 0 && <p className="gameover-newbest">NEW BEST!</p>}
        <p className="gameover-best">Best: {Math.max(best, score)}</p>
        <div className="menu-buttons">
          <GlitchButton label="Play Again" onClick={onPlayAgain} />
          <GlitchButton label="Main Menu" onClick={onMainMenu} />
        </div>
      </div>
    </div>
  );
}
