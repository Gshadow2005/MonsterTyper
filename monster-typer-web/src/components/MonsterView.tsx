import type { Monster } from '../game/types';

const SPRITES: Record<string, string> = {
  none: '/assets/images/MonsterTyper_Zombie.gif',
  boss: '/assets/images/MonsterTyper_Boss.gif',
  jam: '/assets/images/MonsterTyper_JamMonster.gif',
  reverse: '/assets/images/MonsterTyper_Reverse.gif',
  life: '/assets/images/MonsterTyper_Life.gif',
  child: '/assets/images/Bat.gif',
};

const POWER_DOT_COLORS: Record<string, string> = {
  jam: '#ff2222',
  life: '#22cc44',
  reverse: '#3377ff',
  boss: '#eeee33',
};

export function MonsterView({
  monster,
  scale,
  typedText,
}: {
  monster: Monster;
  scale: number;
  typedText: string;
}) {
  let bump = 1;
  if (monster.power === 'jam') bump = 1.3;
  else if (monster.power === 'life' || monster.power === 'reverse') bump = 1.2;

  const drawSize = monster.size * scale * bump;
  const barWidth = Math.max(10, (monster.size - 5) * scale);
  const barHeight = Math.max(2, 3 * scale);
  const fontSize = (monster.isChild ? 13 : 15) * Math.max(0.9, scale);
  const dotSize = 8 * Math.max(0.8, scale);
  const hit = monster.hitFlashTicks > 0;

  const isTarget = typedText.length > 0 && monster.word.startsWith(typedText);
  const isExact = isTarget && monster.word === typedText;

  return (
    <div
      className={`monster ${hit ? 'monster-hit' : ''} ${isTarget ? 'monster-target' : ''} ${isExact ? 'monster-exact' : ''}`}
      style={{
        left: `${monster.relX * 100}%`,
        top: `${monster.relY * 100}%`,
        width: drawSize,
        height: drawSize,
      }}
    >
      <div className="monster-healthbar" style={{ width: barWidth, height: barHeight, top: -(barHeight + 4) }}>
        <div
          className="monster-healthbar-fill"
          style={{
            width: `${(monster.power === 'boss' ? monster.health / 3 : monster.health) * 100}%`,
            backgroundColor: monster.health > 1 ? '#44cc44' : monster.health === 1 ? '#cccc44' : '#cc4444',
          }}
        />
      </div>
      <img
        src={monster.isChild ? SPRITES.child : SPRITES[monster.power]}
        alt=""
        className="monster-sprite"
        draggable={false}
      />
      <div
        className={`monster-word ${hit ? 'monster-word-hit' : ''} ${isTarget ? 'monster-word-target' : ''}`}
        style={{ fontSize, bottom: -fontSize * 1.8 }}
      >
        {monster.word}
      </div>
      {!monster.isChild && POWER_DOT_COLORS[monster.power] && (
        <div
          className="monster-power-dot"
          style={{
            backgroundColor: POWER_DOT_COLORS[monster.power],
            width: dotSize,
            height: dotSize,
            top: -dotSize / 2,
            right: -dotSize / 2,
          }}
        />
      )}
    </div>
  );
}
