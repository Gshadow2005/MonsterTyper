import { useCallback, useEffect, useRef, useState } from 'react';
import { GameEngine } from '../game/engine';
import { makeSnapshot } from '../game/useGameEngine';
import type { GameMode, GameState, LobbyPlayer } from '../game/types';
import * as C from '../game/constants';
import { gameMusic } from '../game/sound';
import { MAX_PLAYERS, makeRoomCode, NetManager, PLAYER_COLORS } from './net';

export type MpPhase = 'idle' | 'connecting' | 'lobby' | 'playing' | 'ended' | 'error';

export function useMultiplayer() {
  const netRef = useRef<NetManager | null>(null);
  const engineRef = useRef<GameEngine | null>(null);
  const stopLoopRef = useRef<(() => void) | null>(null);
  const [phase, setPhase] = useState<MpPhase>('idle');
  const [role, setRole] = useState<'host' | 'client' | null>(null);
  const [code, setCode] = useState('');
  const [players, setPlayers] = useState<LobbyPlayer[]>([]);
  const [mode, setMode] = useState<GameMode>('coop');
  const [snapshot, setSnapshot] = useState<GameState | null>(null);
  const [myId, setMyId] = useState<string | null>(null);
  const [winnerId, setWinnerId] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState('');
  const phaseRef = useRef<MpPhase>('idle');
  const modeRef = useRef<GameMode>('coop');
  const playersRef = useRef<LobbyPlayer[]>([]);
  const hostPlayerIdRef = useRef<string | null>(null);

  const setPhaseSafe = (p: MpPhase) => {
    phaseRef.current = p;
    setPhase(p);
  };

  const stopLoops = useCallback(() => {
    stopLoopRef.current?.();
    stopLoopRef.current = null;
  }, []);

  const teardown = useCallback(() => {
    stopLoops();
    netRef.current?.destroy();
    netRef.current = null;
    engineRef.current = null;
    setSnapshot(null);
    setWinnerId(null);
    setPlayers([]);
    playersRef.current = [];
    setRole(null);
    setMyId(null);
    setPhaseSafe('idle');
  }, [stopLoops]);

  const startHostLoop = useCallback(() => {
    stopLoops();
    const net = netRef.current;
    const engine = engineRef.current;
    if (!net || !engine) return;

    let raf = 0;
    let last = performance.now();
    let accumulator = 0;
    let lastBroadcast = 0;
    let overSent = false;

    const loop = (time: number) => {
      raf = requestAnimationFrame(loop);
      accumulator += Math.min(time - last, 250);
      last = time;
      let steps = 0;
      while (accumulator >= C.TICK_MS && steps < 5) {
        engine.update();
        accumulator -= C.TICK_MS;
        steps += 1;
      }
      const snap = makeSnapshot(engine);
      setSnapshot(snap);
      if (time - lastBroadcast >= 66) {
        lastBroadcast = time;
        net.broadcast({ t: 'snapshot', state: snap });
      }
      if (!engine.gameOver) overSent = false;
      else if (!overSent) {
        overSent = true;
        setWinnerId(engine.winnerId);
        setPhaseSafe('ended');
        net.broadcast({ t: 'snapshot', state: snap });
        gameMusic.stop();
      }
    };
    raf = requestAnimationFrame(loop);
    stopLoopRef.current = () => cancelAnimationFrame(raf);
  }, [stopLoops]);

  const createRoom = useCallback(
    (playerName: string) => {
      teardown();
      const roomCode = makeRoomCode();
      const net = new NetManager();
      netRef.current = net;
      const hostId = `host-${Date.now()}`;
      hostPlayerIdRef.current = hostId;
      const hostPlayer: LobbyPlayer = { id: hostId, name: playerName.slice(0, 12) || 'Host', color: PLAYER_COLORS[0] };
      playersRef.current = [hostPlayer];
      setPlayers(playersRef.current);
      setCode(roomCode);
      setRole('host');
      setMyId(hostId);
      setPhaseSafe('connecting');

      net.onJoinRequest = (peerId, name) => {
        if (phaseRef.current !== 'lobby') {
          net.sendTo(peerId, { t: 'reject', reason: 'Game already in progress' });
          net.closePeer(peerId);
          return;
        }
        if (playersRef.current.length >= MAX_PLAYERS) {
          net.sendTo(peerId, { t: 'reject', reason: 'Room is full (10 players max)' });
          net.closePeer(peerId);
          return;
        }
        const taken = new Set(playersRef.current.map((p) => p.color));
        const color = PLAYER_COLORS.find((c) => !taken.has(c)) ?? PLAYER_COLORS[playersRef.current.length % PLAYER_COLORS.length];
        const joined: LobbyPlayer = { id: peerId, name: (name || 'Player').slice(0, 12), color };
        playersRef.current = [...playersRef.current, joined];
        setPlayers(playersRef.current);
        net.broadcast({ t: 'lobby', players: playersRef.current, mode: modeRef.current });
      };
      net.onPeerLeft = (peerId) => {
        const leaving = playersRef.current.find((p) => p.id === peerId);
        playersRef.current = playersRef.current.filter((p) => p.id !== peerId);
        setPlayers(playersRef.current);
        if (phaseRef.current === 'lobby') {
          net.broadcast({ t: 'lobby', players: playersRef.current, mode: modeRef.current });
        } else if (phaseRef.current === 'playing' && engineRef.current && leaving) {
          engineRef.current.removePlayer(peerId);
        }
      };
      net.onInput = (peerId, text) => {
        engineRef.current?.checkInputFor(peerId, text);
      };
      net.onError = (message) => {
        setErrorMsg(message);
        setPhaseSafe('error');
      };

      net.host(roomCode);
      setPhaseSafe('lobby');
    },
    [teardown],
  );

  const joinRoom = useCallback(
    (roomCode: string, playerName: string) => {
      teardown();
      const clean = roomCode.trim().toUpperCase();
      setCode(clean);
      setRole('client');
      setPhaseSafe('connecting');
      const net = new NetManager();
      netRef.current = net;

      net.onLobby = (lobbyPlayers, lobbyMode) => {
        playersRef.current = lobbyPlayers;
        setPlayers(lobbyPlayers);
        setMode(lobbyMode);
        modeRef.current = lobbyMode;
        setMyId(net.myId);
        setPhaseSafe('lobby');
      };
      net.onStart = () => {
        setSnapshot(null);
        setWinnerId(null);
        setPhaseSafe('playing');
        gameMusic.play();
      };
      net.onSnapshot = (state) => {
        setSnapshot(state);
        if (state.gameOver) {
          setWinnerId(state.winnerId);
          setPhaseSafe('ended');
          gameMusic.stop();
        }
      };
      net.onReject = (reason) => {
        setErrorMsg(reason);
        setPhaseSafe('error');
      };
      net.onHostLeft = () => {
        setErrorMsg('Host closed the room.');
        setPhaseSafe('error');
        gameMusic.stop();
      };
      net.onError = (message) => {
        setErrorMsg(message);
        setPhaseSafe('error');
      };

      net.join(clean, playerName);
    },
    [teardown],
  );

  const startGame = useCallback(() => {
    const net = netRef.current;
    if (!net || role !== 'host' || playersRef.current.length < 1) return;
    const engine = engineRef.current ?? new GameEngine();
    engineRef.current = engine;
    engine.startMultiplayer(modeRef.current, playersRef.current);
    net.broadcast({ t: 'start', mode: modeRef.current });
    setSnapshot(makeSnapshot(engine));
    setWinnerId(null);
    setPhaseSafe('playing');
    gameMusic.play();
    startHostLoop();
  }, [role, startHostLoop]);

  const sendInput = useCallback((text: string) => {
    netRef.current?.sendInput(text);
  }, []);

  const submitHostInput = useCallback((text: string): boolean => {
    const engine = engineRef.current;
    if (!engine || !hostPlayerIdRef.current) return false;
    return engine.checkInputFor(hostPlayerIdRef.current, text);
  }, []);

  const changeMode = useCallback((newMode: GameMode) => {
    modeRef.current = newMode;
    setMode(newMode);
    netRef.current?.broadcast({ t: 'lobby', players: playersRef.current, mode: newMode });
  }, []);

  const leaveRoom = useCallback(() => {
    netRef.current?.sendLeave();
    gameMusic.stop();
    teardown();
  }, [teardown]);

  const rematch = useCallback(() => {
    if (role !== 'host') return;
    const engine = engineRef.current;
    if (!engine) return;
    engine.startMultiplayer(modeRef.current, playersRef.current);
    netRef.current?.broadcast({ t: 'start', mode: modeRef.current });
    setSnapshot(makeSnapshot(engine));
    setWinnerId(null);
    setPhaseSafe('playing');
    gameMusic.play();
  }, [role]);

  useEffect(() => teardown, [teardown]);

  return {
    phase,
    role,
    code,
    players,
    mode,
    snapshot,
    myId,
    winnerId,
    errorMsg,
    createRoom,
    joinRoom,
    startGame,
    sendInput,
    submitHostInput,
    changeMode,
    leaveRoom,
    rematch,
    teardown,
  };
}
