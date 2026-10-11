import { useEffect, useRef, useState } from 'react';
import type { GameEngine } from '../game/engine';
import { sfx } from '../game/sound';

export function InputBar({
  engine,
  jammed,
  scrambled,
  jamMsLeft,
  scrambleMsLeft,
}: {
  engine: GameEngine;
  jammed: boolean;
  scrambled: boolean;
  jamMsLeft: number;
  scrambleMsLeft: number;
}) {
  const [value, setValue] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);
  const jammedRef = useRef(jammed);
  jammedRef.current = jammed;

  useEffect(() => {
    if (engine.clearInputRequested) {
      setValue('');
      engine.clearInputRequested = false;
    }
  });

  useEffect(() => {
    if (engine.gameOver) setValue('');
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const v = e.target.value;
    if (engine.checkInput(v)) {
      setValue('');
      return;
    }
    setValue(v);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && value === '') {
      sfx.click.play();
    }
    if (scrambled && /^[a-zA-Z]$/.test(e.key)) {
      e.preventDefault();
      const pos = e.currentTarget.selectionStart ?? value.length;
      const mapped = engine.scrambleChar(e.key);
      const next = value.slice(0, pos) + mapped + value.slice(pos);
      if (engine.checkInput(next)) setValue('');
      else setValue(next);
      requestAnimationFrame(() => {
        inputRef.current?.setSelectionRange(pos + 1, pos + 1);
      });
    }
  };

  const borderClass = jammed ? 'input-jammed' : scrambled ? 'input-scrambled' : '';
  const placeholder = jammed
    ? `JAMMED — ${(jamMsLeft / 1000).toFixed(1)}s`
    : scrambled
      ? 'SCRAMBLED — type the words anyway…'
      : 'type the monster words…';

  return (
    <div className="input-bar">
      <div className="input-wrap">
        <span className={`status-chip jam ${jammed ? 'on' : ''}`}>
          <span className="chip-dot" />{jammed ? `JAM ${(jamMsLeft / 1000).toFixed(1)}s` : 'JAM'}
        </span>
        <input
          ref={inputRef}
          className={`game-input ${borderClass}`}
          type="text"
          value={value}
          readOnly={jammed}
          spellCheck={false}
          autoComplete="off"
          placeholder={placeholder}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          autoFocus
        />
        <span className={`status-chip scramble ${scrambled ? 'on' : ''}`}>
          <span className="chip-dot" />{scrambled ? `SCRAMBLE ${(scrambleMsLeft / 1000).toFixed(1)}s` : 'SCRAMBLE'}
        </span>
      </div>
    </div>
  );
}
