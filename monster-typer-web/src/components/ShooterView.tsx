import { SHOOTER_X } from '../game/constants';

export function ShooterView({
  angle,
  laserFrame,
  laserActive,
  shake,
  scale,
}: {
  angle: number;
  laserFrame: number;
  laserActive: boolean;
  shake: boolean;
  scale: number;
}) {
  const shooterSize = 46.7 * scale;
  const laserW = 100 * scale;
  const laserH = 60 * scale;

  return (
    <div
      className={`shooter ${shake ? 'shooter-shake' : ''}`}
      style={{
        left: `${SHOOTER_X * 100}%`,
        top: '50%',
        width: shooterSize,
        height: shooterSize,
      }}
    >
      <img
        src="/assets/images/Shooter.gif"
        alt="shooter"
        className="shooter-sprite"
        style={{ transform: `rotate(${angle}deg)` }}
        draggable={false}
      />
      {laserActive && (
        <img
          src={`/assets/Laser/Laser_Beam${laserFrame + 1}.png`}
          alt=""
          className="laser"
          style={{
            width: laserW,
            height: laserH,
            transform: `translateY(-50%) rotate(${angle}deg)`,
            left: shooterSize / 3,
          }}
          draggable={false}
        />
      )}
    </div>
  );
}
