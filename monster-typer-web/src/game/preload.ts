const IMAGES = [
  '/assets/Explosion/Explosion.webp',
  '/assets/Laser/Laser.webp',
  '/assets/images/Bat.gif',
  '/assets/images/MonsterTyper_Zombie.gif',
  '/assets/images/MonsterTyper_Boss.gif',
  '/assets/images/MonsterTyper_JamMonster.gif',
  '/assets/images/MonsterTyper_Life.gif',
  '/assets/images/MonsterTyper_Reverse.gif',
  '/assets/images/Shooter.gif',
];

let started = false;

export function preloadSprites(): void {
  if (started) return;
  started = true;
  for (const src of IMAGES) {
    const img = new Image();
    img.src = src;
  }
}
