import * as C from './constants';
import type { Difficulty, Explosion, Flash, Monster, Notification, PowerType, ShooterState } from './types';
import { createDecks, WordDeck } from './words';
import { sfx } from './sound';

export class GameEngine {
  monsters: Monster[] = [];
  explosions: Explosion[] = [];
  notifications: Notification[] = [];
  flash: Flash | null = null;
  shooter: ShooterState = {
    targetId: null,
    angle: 0,
    laserTicksLeft: 0,
    attackTicksLeft: 0,
    laserFrame: 0,
    shakeTicksLeft: 0,
    recenterAt: null,
  };

  score = 0;
  lives = C.INITIAL_LIVES;
  streak = 0;
  difficulty: Difficulty = 'easy';
  speed = C.MONSTER_INITIAL_SPEED;
  frozen = false;
  jammed = false;
  scrambled = false;
  running = false;
  paused = false;
  gameOver = false;
  clearInputRequested = false;
  typedText = '';

  private now = 0;
  private nextId = 1;
  private nextNotifId = 1;
  private frozenUntil = 0;
  private jammedUntil = 0;
  private scrambledUntil = 0;
  private scrambleMap = new Map<string, string>();
  private clearInputAt: number | null = null;
  private decks: Record<Difficulty, WordDeck> = createDecks();

  start(): void {
    this.reset();
    this.running = true;
    this.spawnMonster();
  }

  reset(): void {
    this.monsters = [];
    this.explosions = [];
    this.notifications = [];
    this.flash = null;
    this.decks = createDecks();
    this.shooter = { targetId: null, angle: 0, laserTicksLeft: 0, attackTicksLeft: 0, laserFrame: 0, shakeTicksLeft: 0, recenterAt: null };
    this.score = 0;
    this.lives = C.INITIAL_LIVES;
    this.streak = 0;
    this.difficulty = 'easy';
    this.speed = C.MONSTER_INITIAL_SPEED;
    this.frozen = false;
    this.jammed = false;
    this.scrambled = false;
    this.running = false;
    this.paused = false;
    this.gameOver = false;
    this.clearInputRequested = false;
    this.typedText = '';
    this.now = 0;
    this.frozenUntil = 0;
    this.jammedUntil = 0;
    this.scrambledUntil = 0;
    this.clearInputAt = null;
    this.pendingHitId = null;
  }

  togglePause(): void {
    if (!this.running || this.gameOver) return;
    this.paused = !this.paused;
  }

  update(): void {
    if (!this.running || this.paused || this.gameOver) return;
    this.now += C.TICK_MS;

    this.updateTimers();
    if (!this.frozen) this.moveMonsters();
    this.checkBreaches();
    if (this.running) this.maybeSpawn();

    if (this.shooter.laserTicksLeft > 0) {
      this.shooter.laserTicksLeft -= 1;
      this.shooter.laserFrame = (this.shooter.laserFrame + 1) % C.LASER_FRAMES;
    }
    if (this.shooter.attackTicksLeft > 0) {
      this.shooter.attackTicksLeft -= 1;
      if (this.shooter.attackTicksLeft === 0) this.resolvePendingHit();
    }
    if (this.shooter.recenterAt !== null && this.now >= this.shooter.recenterAt) {
      this.shooter.recenterAt = null;
      this.shooter.targetId = null;
    }
    if (this.shooter.shakeTicksLeft > 0) this.shooter.shakeTicksLeft -= 1;

    this.updateExplosions();
  }

  private updateTimers(): void {
    if (this.frozen && this.now >= this.frozenUntil) this.frozen = false;
    if (this.jammed && this.now >= this.jammedUntil) this.jammed = false;
    if (this.scrambled && this.now >= this.scrambledUntil) this.scrambled = false;

    if (this.clearInputAt !== null && this.now >= this.clearInputAt) {
      this.clearInputRequested = true;
      this.clearInputAt = null;
    }
    this.notifications = this.notifications.filter((n) => this.now < n.expiresAt);
    if (this.flash && this.now >= this.flash.expiresAt) this.flash = null;
  }

  private moveMonsters(): void {
    const tuning = C.DIFFICULTY_TUNING[this.difficulty];
    for (const m of this.monsters) {
      if (m.hitFlashTicks > 0) m.hitFlashTicks -= 1;
      let pixels = this.speed * tuning.speed;
      if (m.isChild) pixels *= 2;
      else if (m.power === 'reverse') pixels *= 1.5;
      m.relX -= pixels / C.REF_WIDTH;
    }
    this.enforceLaneSeparation();
  }

  private enforceLaneSeparation(): void {
    const lanes: Monster[][] = Array.from({ length: C.LANE_COUNT }, () => []);
    for (const m of this.monsters) lanes[m.lane].push(m);
    for (const lane of lanes) {
      lane.sort((a, b) => a.relX - b.relX);
      for (let i = 1; i < lane.length; i++) {
        const front = lane[i - 1];
        const back = lane[i];
        const gap = (front.size + back.size) / 2 / C.REF_WIDTH;
        const minX = front.relX + gap;
        if (back.relX < minX) back.relX = Math.min(0.98, minX);
      }
    }
  }

  private checkBreaches(): void {
    const breached = this.monsters.filter((m) => m.relX <= C.BASE_THRESHOLD);
    if (breached.length === 0) return;
    for (const m of breached) {
      const loss = m.health > 1 ? C.BOSS_BREACH_LIVES_PENALTY : 1;
      this.lives = Math.max(0, this.lives - loss);
      this.notify(`-${loss} Life!`, '#ff4444');
      this.streak = 0;
    }
    const breachedIds = new Set(breached.map((m) => m.id));
    this.monsters = this.monsters.filter((m) => !breachedIds.has(m.id));
    if (this.lives <= 0) this.endGame();
  }

  private maybeSpawn(): void {
    if (this.monsters.length >= C.MAX_MONSTERS) return;
    const tuning = C.DIFFICULTY_TUNING[this.difficulty];
    if (Math.random() * 100 < C.SPAWN_CHANCE * tuning.spawnRate) this.spawnMonster();
  }

  private liveWords(): Set<string> {
    const set = new Set<string>();
    for (const m of this.monsters) {
      set.add(m.word);
      if (m.secondWord) set.add(m.secondWord);
      if (m.thirdWord) set.add(m.thirdWord);
    }
    return set;
  }

  private pickLane(): number {
    const counts = new Array<number>(C.LANE_COUNT).fill(0);
    for (const m of this.monsters) counts[m.lane] += 1;
    const min = Math.min(...counts);
    const best = counts.reduce<number[]>((acc, c, i) => (c === min ? [...acc, i] : acc), []);
    return best[Math.floor(Math.random() * best.length)];
  }

  spawnMonster(): void {
    const relX = 0.95;
    const lane = this.pickLane();
    const relY = Math.min(0.93, Math.max(0.02, (lane + 0.5) / C.LANE_COUNT + (Math.random() - 0.5) * 0.02));

    const tuning = C.DIFFICULTY_TUNING[this.difficulty];
    const roll = Math.floor(Math.random() * 100);
    let power: PowerType = 'none';

    const bossCount = this.monsters.filter((m) => m.power === 'boss').length;
    const bossAllowed = tuning.bossChance > 0 && bossCount < C.MAX_BOSSES;

    if (bossAllowed && roll < tuning.bossChance) power = 'boss';
    else if (roll < C.SPLIT_CHANCE + C.JAM_POWER_CHANCE) power = 'jam';
    else if (roll < C.SPLIT_CHANCE + C.JAM_POWER_CHANCE + C.REVERSE_POWER_CHANCE) power = 'reverse';
    else if (roll < C.SPLIT_CHANCE + C.JAM_POWER_CHANCE + C.REVERSE_POWER_CHANCE + C.EXTRA_LIFE_CHANCE) power = 'life';

    const avoid = this.liveWords();
    const word = this.decks[this.difficulty].draw(avoid);
    const monster: Monster = {
      id: this.nextId++,
      relX,
      relY,
      lane,
      word,
      secondWord: null,
      thirdWord: null,
      power,
      health: 1,
      size: C.MONSTER_SIZE,
      isChild: false,
      hitFlashTicks: 0,
    };

    if (power === 'boss') {
      monster.health = C.BOSS_HEALTH;
      monster.size = C.MONSTER_SIZE * 3.5;
      avoid.add(word);
      monster.secondWord = this.decks.medium.draw(avoid);
      avoid.add(monster.secondWord);
      monster.thirdWord = this.decks.medium.draw(avoid);
    } else if (power === 'reverse') {
      monster.size = C.MONSTER_SIZE * 1.3;
    } else if (power === 'life') {
      monster.size = C.MONSTER_SIZE * 0.8;
    }

    this.monsters.push(monster);
  }

  checkInput(rawText: string): boolean {
    if (!this.running || this.paused || this.gameOver) {
      this.typedText = '';
      return false;
    }
    const text = rawText.trim().toLowerCase();
    this.typedText = text;
    if (!text) return false;

    const target = this.monsters.find((m) => m.word === text);
    if (target) {
      this.registerPerfectHit();
      this.fire(target);
      this.typedText = '';
      this.clearInputRequested = true;
      this.clearInputAt = null;
      return true;
    }
    const isPartial = this.monsters.some((m) => m.word.startsWith(text));
    if (!isPartial) {
      this.resetStreak();
      this.clearInputAt = this.now + C.CLEAR_INPUT_TIMEOUT;
    }
    return false;
  }

  private registerPerfectHit(): void {
    this.streak += 1;
    if (this.streak >= C.PERFECT_HITS_FOR_POWERUP) {
      this.streak = 0;
      this.activateRandomPowerUp();
    }
  }

  resetStreak(): void {
    this.streak = 0;
  }

  private fire(monster: Monster): void {
    this.shooter.targetId = monster.id;
    this.shooter.laserTicksLeft = C.LASER_TICKS;
    this.shooter.attackTicksLeft = C.ATTACK_TICKS;
    this.shooter.laserFrame = 0;
    this.shooter.recenterAt = null;
    sfx.gun.play();
    this.pendingHitId = monster.id;
  }

  private pendingHitId: number | null = null;

  private resolvePendingHit(): void {
    if (this.pendingHitId === null) return;
    const monster = this.monsters.find((m) => m.id === this.pendingHitId);
    this.pendingHitId = null;
    if (monster) this.hitMonster(monster);
    this.shooter.recenterAt = this.now + C.SHOOTER_RECENTER_DELAY;
  }

  private hitMonster(monster: Monster): void {
    monster.health -= 1;
    monster.hitFlashTicks = C.HIT_FLASH_TICKS;
    this.shooter.shakeTicksLeft = C.SHAKE_TICKS;
    sfx.hurt.play();

    if (monster.health > 0) {
      if (monster.power === 'boss') {
        if (monster.health === 2 && monster.secondWord) monster.word = monster.secondWord;
        else if (monster.health === 1 && monster.thirdWord) monster.word = monster.thirdWord;
      }
      return;
    }

    if (monster.power === 'boss') this.splitBoss(monster);
    if (monster.power === 'jam') this.startKeyboardJam();
    if (monster.power === 'life') this.increaseLife();
    if (monster.power === 'reverse') this.startInputScramble();

    this.addExplosion(monster.relX, monster.relY);
    this.monsters = this.monsters.filter((m) => m.id !== monster.id);
    this.increaseScore();
    this.clearInputRequested = true;
  }

  private splitBoss(boss: Monster): void {
    const childSize = boss.size * 0.3;
    const avoid = this.liveWords();
    for (let i = 0; i < C.BOSS_CHILD_COUNT; i++) {
      const angle = (Math.PI * i) / C.BOSS_CHILD_COUNT;
      const distance = C.BOSS_CHILD_SPREAD * (0.5 + 0.3 * Math.random());
      const relX = Math.min(0.95, Math.max(0.05, boss.relX + distance * Math.cos(angle)));
      const relY = Math.min(0.93, Math.max(0.02, boss.relY + distance * Math.sin(angle)));
      const lane = Math.min(C.LANE_COUNT - 1, Math.max(0, Math.floor(relY * C.LANE_COUNT)));
      const word = this.decks.medium.draw(avoid);
      avoid.add(word);
      this.monsters.push({
        id: this.nextId++,
        relX,
        relY: (lane + 0.5) / C.LANE_COUNT,
        lane,
        word,
        secondWord: null,
        thirdWord: null,
        power: 'none',
        health: 1,
        size: childSize,
        isChild: true,
        hitFlashTicks: 0,
      });
    }
  }

  increaseScore(): void {
    this.score += C.SCORE_PER_MONSTER;
    this.speed = Math.min(C.MONSTER_MAX_SPEED, this.speed + C.SPEED_INCREASE_PER_KILL);
    this.updateDifficulty();
  }

  increaseLife(): void {
    this.lives += 1;
    this.notify('+1 Life!', '#44ff66');
  }

  private updateDifficulty(): void {
    let next: Difficulty = 'easy';
    if (this.score >= C.HARD_DIFFICULTY_THRESHOLD) next = 'hard';
    else if (this.score >= C.MEDIUM_DIFFICULTY_THRESHOLD) next = 'medium';

    if (next !== this.difficulty) {
      this.difficulty = next;
      const label = next === 'hard' ? 'Hard' : 'Medium';
      const color = next === 'hard' ? '#ff4444' : '#44ff44';
      this.notify(`Difficulty increased to ${label}!`, color);
    }
  }

  startKeyboardJam(): void {
    this.jammedUntil = this.now + C.JAM_DURATION;
    this.jammed = true;
    this.clearInputRequested = true;
    this.notify('Keyboard Jammed!', '#ff4444');
  }

  startInputScramble(): void {
    this.scrambledUntil = this.now + C.SCRAMBLE_DURATION;
    this.scrambled = true;
    this.scrambleMap = buildScrambleMap();
    this.notify('Input Scrambled!', '#4488ff');
  }

  scrambleChar(ch: string): string {
    const lower = ch.toLowerCase();
    const mapped = this.scrambleMap.get(lower);
    if (!mapped) return ch;
    return ch === ch.toUpperCase() && ch !== ch.toLowerCase() ? mapped.toUpperCase() : mapped;
  }

  private activateRandomPowerUp(): void {
    const choices: Array<() => void> = [
      () => this.activateFreeze(),
      () => this.activateSkipWord(),
      () => this.activateKillAll(),
    ];
    choices[Math.floor(Math.random() * choices.length)]();
  }

  private activateFreeze(): void {
    this.frozenUntil = this.now + C.FREEZE_DURATION;
    this.frozen = true;
    this.flash = { color: 'rgba(200,230,255,0.35)', expiresAt: this.now + C.FREEZE_DURATION };
    sfx.freezing.play();
    this.notify('Freeze Activated!', '#66ccff');
  }

  private activateSkipWord(): void {
    if (this.monsters.length <= 1) {
      this.notify('No Other Words to Skip!', '#aaaaaa');
      return;
    }
    const others = this.monsters.slice(1);
    const victim = others[Math.floor(Math.random() * others.length)];
    this.addExplosion(victim.relX, victim.relY);
    this.monsters = this.monsters.filter((m) => m.id !== victim.id);
    this.flash = { color: 'rgba(0,255,0,0.25)', expiresAt: this.now + C.POWERUP_FLASH_DURATION };
    sfx.skip.play();
    this.increaseScore();
    this.notify('Word Skipped!', '#44ff44');
  }

  private activateKillAll(): void {
    for (const m of this.monsters) this.addExplosion(m.relX, m.relY);
    const count = this.monsters.length;
    this.monsters = [];
    this.flash = { color: 'rgba(255,0,0,0.25)', expiresAt: this.now + C.POWERUP_FLASH_DURATION };
    sfx.execution.play();
    for (let i = 0; i < count; i++) this.increaseScore();
    this.notify('All Monsters Defeated!', '#ff4444');
  }

  private addExplosion(relX: number, relY: number): void {
    this.explosions.push({ id: this.nextId++, relX, relY, frame: 0, ticks: 0 });
  }

  private updateExplosions(): void {
    for (const e of this.explosions) {
      e.ticks += 1;
      if (e.ticks % C.EXPLOSION_TICKS_PER_FRAME === 0) e.frame += 1;
    }
    this.explosions = this.explosions.filter((e) => e.frame < C.EXPLOSION_FRAMES);
  }

  private notify(text: string, color: string): void {
    this.notifications.push({ id: this.nextNotifId++, text, color, expiresAt: this.now + C.NOTIFICATION_DURATION });
  }

  private endGame(): void {
    this.running = false;
    this.gameOver = true;
    this.jammed = false;
    this.scrambled = false;
  }

  get freezeMsLeft(): number {
    return this.frozen ? Math.max(0, this.frozenUntil - this.now) : 0;
  }

  get jamMsLeft(): number {
    return this.jammed ? Math.max(0, this.jammedUntil - this.now) : 0;
  }

  get scrambleMsLeft(): number {
    return this.scrambled ? Math.max(0, this.scrambledUntil - this.now) : 0;
  }
}

function buildScrambleMap(): Map<string, string> {
  const shift = Math.floor(Math.random() * 10) + 5;
  const map = new Map<string, string>();
  for (let i = 0; i < 26; i++) {
    const original = String.fromCharCode(97 + i);
    const scrambled = String.fromCharCode(97 + ((i + shift) % 26));
    map.set(original, scrambled);
  }
  return map;
}
