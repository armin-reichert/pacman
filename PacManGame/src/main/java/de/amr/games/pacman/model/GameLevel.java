package de.amr.games.pacman.model;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	public final Symbol bonusSymbol;
	public final int bonusValue;
	public final float pacManSpeed;
	public final float pacManDotsSpeed;
	public final float ghostSpeed;
	public final float ghostTunnelSpeed;
	public final int elroy1DotsLeft;
	public final float elroy1Speed;
	public final int elroy2DotsLeft;
	public final float elroy2Speed;
	public final float pacManPowerSpeed;
	public final float pacManPowerDotsSpeed;
	public final float ghostFrightenedSpeed;
	public final int pacManPowerSeconds;
	public final int mazeNumFlashes;

	public int number;
	public int numPelletsEaten;
	public int ghostsKilledByEnergizer;
	public int ghostsKilledInLevel;

	public GameLevel(Symbol bonusSymbol, int bonusValue, float pacManSpeed, float pacManDotsSpeed, float ghostSpeed,
			float ghostTunnelSpeed, int elroy1DotsLeft, float elroy1Speed, int elroy2DotsLeft, float elroy2Speed,
			float pacManPowerSpeed, float pacManPowerDotsSpeed, float ghostFrightenedSpeed, int pacManPowerSeconds,
			int mazeNumFlashes) {
		this.bonusSymbol = bonusSymbol;
		this.bonusValue = bonusValue;
		this.pacManSpeed = pacManSpeed;
		this.pacManDotsSpeed = pacManPowerDotsSpeed;
		this.ghostSpeed = ghostSpeed;
		this.ghostTunnelSpeed = ghostTunnelSpeed;
		this.elroy1DotsLeft = elroy1DotsLeft;
		this.elroy1Speed = elroy1Speed;
		this.elroy2DotsLeft = elroy2DotsLeft;
		this.elroy2Speed = elroy2Speed;
		this.pacManPowerSpeed = pacManPowerSpeed;
		this.pacManPowerDotsSpeed = pacManPowerDotsSpeed;
		this.ghostFrightenedSpeed = ghostFrightenedSpeed;
		this.pacManPowerSeconds = pacManPowerSeconds;
		this.mazeNumFlashes = mazeNumFlashes;
	}
}