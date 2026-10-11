import { useRef, useState } from 'react';

export function GlitchButton({
  label,
  onClick,
}: {
  label: string;
  onClick: () => void;
}) {
  const [text, setText] = useState(label);
  const [color, setColor] = useState('');
  const glitchTimer = useRef<number | null>(null);
  const flickerTimer = useRef<number | null>(null);

  const SYMBOLS = '!@#$%oten^&*<>|/\\';

  const startGlitch = () => {
    if (glitchTimer.current !== null) return;
    glitchTimer.current = window.setInterval(() => {
      if (Math.random() < 0.4) {
        const chars = label.split('');
        const count = 1 + Math.floor(Math.random() * 2);
        for (let i = 0; i < count; i++) {
          const idx = Math.floor(Math.random() * chars.length);
          chars[idx] = SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
        }
        setText(chars.join(''));
        window.setTimeout(() => setText(label), 120);
      }
    }, 100);
    flickerTimer.current = window.setInterval(() => {
      if (Math.random() < 0.1) {
        setColor('red');
        window.setTimeout(() => setColor(''), 50);
      }
    }, 150);
  };

  const stopGlitch = () => {
    if (glitchTimer.current !== null) window.clearInterval(glitchTimer.current);
    if (flickerTimer.current !== null) window.clearInterval(flickerTimer.current);
    glitchTimer.current = null;
    flickerTimer.current = null;
    setText(label);
    setColor('');
  };

  return (
    <button
      className="menu-button"
      style={color ? { backgroundColor: color, color: '#fff' } : undefined}
      onMouseEnter={startGlitch}
      onMouseLeave={stopGlitch}
      onClick={onClick}
    >
      {text}
    </button>
  );
}
