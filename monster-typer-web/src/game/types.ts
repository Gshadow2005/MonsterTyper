export type PowerType = 'none' | 'jam' | 'reverse' | 'life' | 'boss';

export type Difficulty = 'easy' | 'medium' | 'hard';

export interface Monster {
  id: number;
  relX: number;
  relY: number;
  lane: number;
  word: string;
  secondWord: string | null;
  thirdWord: string | null;
  power: PowerType;
  health: number;
  size: number;
  isChild: boolean;
  hitFlashTicks: number;
}

export interface Explosion {
  id: number;
  relX: number;
  relY: number;
  frame: number;
  ticks: number;
}

export interface Notification {
  id: number;
  text: string;
  color: string;
  expiresAt: number;
}

export interface Flash {
  color: string;
  expiresAt: number;
}

export interface ShooterState {
  targetId: number | null;
  angle: number;
  laserTicksLeft: number;
  attackTicksLeft: number;
  laserFrame: number;
  shakeTicksLeft: number;
  recenterAt: number | null;
}

export interface GameState {
  monsters: Monster[];
  explosions: Explosion[];
  score: number;
  lives: number;
  streak: number;
  difficulty: Difficulty;
  frozen: boolean;
  jammed: boolean;
  scrambled: boolean;
  freezeMsLeft: number;
  jamMsLeft: number;
  scrambleMsLeft: number;
  notifications: Notification[];
  flash: Flash | null;
  shooter: ShooterState;
  running: boolean;
  paused: boolean;
  gameOver: boolean;
  typedText: string;
}

export interface HudState {
  score: number;
  lives: number;
  streak: number;
  best: number;
}
