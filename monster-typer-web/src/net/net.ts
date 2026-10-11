import Peer from 'peerjs';
import type { DataConnection } from 'peerjs';
import type { GameMode, GameState, LobbyPlayer } from '../game/types';

export const MAX_PLAYERS = 10;

export const PLAYER_COLORS = [
  '#ff5555',
  '#55aaff',
  '#55dd66',
  '#ffcc33',
  '#cc66ff',
  '#ff8844',
  '#44e0d0',
  '#ff66aa',
  '#99dd33',
  '#aa88ff',
];

const CODE_CHARS = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789';

export function makeRoomCode(): string {
  let code = '';
  for (let i = 0; i < 4; i++) code += CODE_CHARS[Math.floor(Math.random() * CODE_CHARS.length)];
  return code;
}

export type NetMessage =
  | { t: 'join'; name: string }
  | { t: 'lobby'; players: LobbyPlayer[]; mode: GameMode }
  | { t: 'start'; mode: GameMode }
  | { t: 'snapshot'; state: GameState }
  | { t: 'reject'; reason: string }
  | { t: 'input'; text: string }
  | { t: 'leave' };

export class NetManager {
  private peer: Peer | null = null;
  private conns = new Map<string, DataConnection>();
  private hostConn: DataConnection | null = null;

  role: 'host' | 'client' | null = null;
  myId: string | null = null;

  onJoinRequest: ((id: string, name: string) => void) | null = null;
  onPeerLeft: ((id: string) => void) | null = null;
  onInput: ((id: string, text: string) => void) | null = null;
  onLobby: ((players: LobbyPlayer[], mode: GameMode) => void) | null = null;
  onStart: ((mode: GameMode) => void) | null = null;
  onSnapshot: ((state: GameState) => void) | null = null;
  onReject: ((reason: string) => void) | null = null;
  onHostLeft: (() => void) | null = null;
  onError: ((message: string) => void) | null = null;

  host(code: string): void {
    this.destroy();
    this.role = 'host';
    const peer = new Peer(`mt-${code}`);
    this.peer = peer;
    peer.on('open', (id) => {
      this.myId = id;
    });
    peer.on('connection', (conn) => this.setupHostConn(conn));
    peer.on('error', (err) => {
      if (err.type === 'unavailable-id') this.onError?.('Could not create room (code taken) — try again.');
      else this.onError?.('Connection error — check your network.');
    });
  }

  private setupHostConn(conn: DataConnection): void {
    conn.on('open', () => {
      this.conns.set(conn.peer, conn);
    });
    conn.on('data', (data) => {
      const msg = data as NetMessage;
      if (msg.t === 'join') this.onJoinRequest?.(conn.peer, msg.name);
      else if (msg.t === 'input') this.onInput?.(conn.peer, msg.text);
      else if (msg.t === 'leave') {
        this.conns.delete(conn.peer);
        this.onPeerLeft?.(conn.peer);
      }
    });
    const drop = () => {
      if (this.conns.delete(conn.peer)) this.onPeerLeft?.(conn.peer);
    };
    conn.on('close', drop);
    conn.on('error', drop);
  }

  join(code: string, name?: string): void {
    this.destroy();
    this.role = 'client';
    const peer = new Peer();
    this.peer = peer;
    peer.on('error', (err) => {
      if (err.type === 'peer-unavailable') this.onError?.(`Room ${code} not found.`);
      else this.onError?.('Connection error — check your network.');
    });
    peer.on('open', (id) => {
      this.myId = id;
      const conn = peer.connect(`mt-${code}`, { reliable: true });
      this.hostConn = conn;
      conn.on('open', () => {
        if (name) conn.send({ t: 'join', name } satisfies NetMessage);
      });
      conn.on('data', (data) => {
        const msg = data as NetMessage;
        if (msg.t === 'lobby') this.onLobby?.(msg.players, msg.mode);
        else if (msg.t === 'start') this.onStart?.(msg.mode);
        else if (msg.t === 'snapshot') this.onSnapshot?.(msg.state);
        else if (msg.t === 'reject') this.onReject?.(msg.reason);
      });
      conn.on('close', () => this.onHostLeft?.());
      conn.on('error', () => this.onHostLeft?.());
    });
  }

  sendToHost(msg: NetMessage): void {
    if (this.hostConn?.open) this.hostConn.send(msg);
  }

  sendInput(text: string): void {
    this.sendToHost({ t: 'input', text });
  }

  sendLeave(): void {
    this.sendToHost({ t: 'leave' });
  }

  broadcast(msg: NetMessage): void {
    for (const conn of this.conns.values()) {
      if (conn.open) conn.send(msg);
    }
  }

  sendTo(id: string, msg: NetMessage): void {
    const conn = this.conns.get(id);
    if (conn?.open) conn.send(msg);
  }

  closePeer(id: string): void {
    const conn = this.conns.get(id);
    if (conn) {
      this.conns.delete(id);
      conn.close();
    }
  }

  destroy(): void {
    for (const conn of this.conns.values()) conn.close();
    this.conns.clear();
    this.hostConn?.close();
    this.hostConn = null;
    this.peer?.destroy();
    this.peer = null;
    this.role = null;
    this.myId = null;
  }
}
