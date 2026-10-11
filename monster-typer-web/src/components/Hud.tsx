import type { Difficulty } from '../game/types';

const DIFFICULTY_LABEL: Record<Difficulty, string> = { easy: 'EASY', medium: 'MEDIUM', hard: 'HARD' };

export function Hud({
  score,
  lives,
  streak,
  best,
  difficulty,
  onBackToMenu,
}: {
  score: number;
  lives: number;
  streak: number;
  best: number;
  difficulty: Difficulty;
  onBackToMenu: () => void;
}) {
  return (
    <div className="hud">
      <div className="hud-group">
        <div className="hud-label">Score</div>
        <div className="hud-score" key={`s-${score}`}>{score}</div>
      </div>

      <div className="hud-group hud-streak-group">
        <div className="hud-label">
          Streak <span className={`streak-count ${streak >= 8 ? 'streak-hot' : ''}`}>{streak}/10</span>
        </div>
        <div className="streak-bar">
          <div className={`streak-fill ${streak >= 8 ? 'streak-hot-fill' : ''}`} style={{ width: `${(streak / 10) * 100}%` }} />
        </div>
      </div>

      <div className="hud-group">
        <div className="hud-label">Lives</div>
        <div className="hud-lives" key={`l-${lives}`}>
          <span className="hearts">{lives > 0 ? '♥'.repeat(Math.min(lives, 10)) : '—'}</span>
          {lives > 10 && <span className="lives-extra">+{lives - 10}</span>}
        </div>
      </div>

      <div className="hud-group">
        <div className="hud-label">Difficulty</div>
        <div className={`difficulty-badge difficulty-${difficulty}`}>{DIFFICULTY_LABEL[difficulty]}</div>
      </div>

      <div className="hud-group">
        <div className="hud-label">Best</div>
        <div className="hud-best">{Math.max(best, score)}</div>
      </div>

      <button className="back-button" onClick={onBackToMenu}>
        Menu
      </button>
    </div>
  );
}
