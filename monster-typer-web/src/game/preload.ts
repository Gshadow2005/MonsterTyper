const FRAMES = [
  ...Array.from({ length: 12 }, (_, i) => `/assets/Explosion/Explosion${i + 1}.png`),
  ...Array.from({ length: 14 }, (_, i) => `/assets/Laser/Laser_Beam${i + 1}.png`),
];

let started = false;

export function preloadFrames(): void {
  if (started) return;
  started = true;
  for (const src of FRAMES) {
    const img = new Image();
    img.src = src;
  }
}
