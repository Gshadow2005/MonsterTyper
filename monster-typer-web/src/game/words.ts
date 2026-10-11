import easyRaw from '../data/easy_words.txt?raw';
import mediumRaw from '../data/medium_words.txt?raw';
import hardRaw from '../data/hard_words.txt?raw';
import type { Difficulty } from './types';

function parseWords(raw: string): string[] {
  return [...new Set(raw.split(/\r?\n/).map((w) => w.trim().toLowerCase()).filter(Boolean))];
}

export const WORDS: Record<Difficulty, string[]> = {
  easy: parseWords(easyRaw),
  medium: parseWords(mediumRaw),
  hard: parseWords(hardRaw),
};

export class WordDeck {
  private all: string[];
  private queue: string[] = [];

  constructor(all: string[]) {
    this.all = all;
    this.reshuffle();
  }

  private reshuffle(): void {
    this.queue = [...this.all];
    for (let i = this.queue.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [this.queue[i], this.queue[j]] = [this.queue[j], this.queue[i]];
    }
  }

  draw(avoid?: Set<string>): string {
    for (let attempt = 0; attempt < 60; attempt++) {
      if (this.queue.length === 0) this.reshuffle();
      const word = this.queue.pop() as string;
      if (!avoid || !avoid.has(word)) return word;
    }
    if (this.queue.length === 0) this.reshuffle();
    return this.queue.pop() as string;
  }
}

export function createDecks(): Record<Difficulty, WordDeck> {
  return {
    easy: new WordDeck(WORDS.easy),
    medium: new WordDeck(WORDS.medium),
    hard: new WordDeck(WORDS.hard),
  };
}
