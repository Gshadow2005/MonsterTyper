import type { Difficulty } from './types';

export const REF_WIDTH = 800;
export const REF_HEIGHT = 700;

export const TICK_MS = 16;

export const MONSTER_SIZE = 50;
export const MONSTER_INITIAL_SPEED = 0.5;
export const MONSTER_MAX_SPEED = 10;
export const SPEED_INCREASE_PER_KILL = 0.01;

export const SPLIT_CHANCE = 5;
export const JAM_POWER_CHANCE = 3;
export const REVERSE_POWER_CHANCE = 5;
export const EXTRA_LIFE_CHANCE = 6;

export const MAX_BOSSES = 1;

export const JAM_DURATION = 1200;
export const SCRAMBLE_DURATION = 5000;

export const INITIAL_LIVES = 5;
export const SCORE_PER_MONSTER = 10;

export interface DifficultyTuning {
  spawnRate: number;
  speed: number;
  bossChance: number;
}

export const DIFFICULTY_TUNING: Record<Difficulty, DifficultyTuning> = {
  easy: { spawnRate: 0.7, speed: 0.85, bossChance: 1.5 },
  medium: { spawnRate: 1.0, speed: 1.0, bossChance: 3 },
  hard: { spawnRate: 1.45, speed: 1.3, bossChance: 5 },
};

export const SPAWN_CHANCE = 1;

export const LANE_COUNT = 6;
export const MAX_MONSTERS = 14;

export const MEDIUM_DIFFICULTY_THRESHOLD = 200;
export const HARD_DIFFICULTY_THRESHOLD = 400;

export const FREEZE_DURATION = 3000;
export const PERFECT_HITS_FOR_POWERUP = 10;
export const CLEAR_INPUT_TIMEOUT = 2500;

export const BOSS_BREACH_LIVES_PENALTY = 3;
export const BOSS_HEALTH = 3;
export const BOSS_CHILD_COUNT = 5;
export const BOSS_CHILD_SPREAD = 0.15;

export const ATTACK_TICKS = 10;
export const LASER_TICKS = 14;
export const LASER_FRAMES = 14;
export const EXPLOSION_FRAMES = 12;
export const EXPLOSION_TICKS_PER_FRAME = 3;
export const SHAKE_TICKS = 20;
export const HIT_FLASH_TICKS = 5;
export const SHOOTER_RECENTER_DELAY = 1000;

export const NOTIFICATION_DURATION = 2000;
export const POWERUP_FLASH_DURATION = 500;

export const BASE_THRESHOLD = 0.06;
export const SHOOTER_X = 0.042;

export const HIGH_SCORE_KEY = 'monsterTyperHighScore';
