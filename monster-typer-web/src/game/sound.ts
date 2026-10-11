const ASSETS = '/assets/Sounds';

class SoundPool {
  private clips: HTMLAudioElement[];
  private index = 0;

  constructor(src: string, volume: number, size = 3) {
    this.clips = Array.from({ length: size }, () => {
      const a = new Audio(src);
      a.volume = volume;
      return a;
    });
  }

  play(): void {
    const clip = this.clips[this.index];
    this.index = (this.index + 1) % this.clips.length;
    clip.currentTime = 0;
    void clip.play().catch(() => {});
  }
}

export const sfx = {
  gun: new SoundPool(`${ASSETS}/GunSound.wav`, 0.5),
  click: new SoundPool(`${ASSETS}/Click.wav`, 0.4),
  hurt: new SoundPool(`${ASSETS}/HurtSound.wav`, 0.6),
  execution: new SoundPool(`${ASSETS}/Execution.wav`, 0.6),
  freezing: new SoundPool(`${ASSETS}/Freezing.wav`, 0.6),
  skip: new SoundPool(`${ASSETS}/Skip1.wav`, 0.6),
  gameOver: new SoundPool(`${ASSETS}/GameOverSound.wav`, 0.7),
};

class Music {
  private audio: HTMLAudioElement;

  constructor(src: string, volume: number) {
    this.audio = new Audio(src);
    this.audio.loop = true;
    this.audio.volume = volume;
  }

  play(): void {
    void this.audio.play().catch(() => {});
  }

  stop(): void {
    this.audio.pause();
    this.audio.currentTime = 0;
  }
}

export const menuMusic = new Music(`${ASSETS}/MainMenu.mp3`, 0.5);
export const gameMusic = new Music(`${ASSETS}/Backround.mp3`, 0.4);
