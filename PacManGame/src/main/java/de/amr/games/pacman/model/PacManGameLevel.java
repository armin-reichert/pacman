package de.amr.games.pacman.model;

/**
 * Data structure storing the level-specific parameters.
 * 
 * @author Armin Reichert
 */
public class PacManGameLevel {

	public final BonusSymbol bonusSymbol;
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

	public PacManGameLevel(Object[] row) {
		bonusSymbol = (BonusSymbol) row[0];
		bonusValue = (int) row[1];
		pacManSpeed = (float) row[2];
		pacManDotsSpeed = (float) row[3];
		ghostSpeed = (float) row[4];
		ghostTunnelSpeed = (float) row[5];
		elroy1DotsLeft = (int) row[6];
		elroy1Speed = (float) row[7];
		elroy2DotsLeft = (int) row[8];
		elroy2Speed = (float) row[9];
		pacManPowerSpeed = (float) row[10];
		pacManPowerDotsSpeed = (float) row[11];
		ghostFrightenedSpeed = (float) row[12];
		pacManPowerSeconds = (int) row[13];
		mazeNumFlashes = (int) row[14];
	}
}