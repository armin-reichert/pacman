package de.amr.games.pacman.model;

import de.amr.games.pacman.model.world.Symbol;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {

	public Symbol bonusSymbol;
	public int bonusValue;
	public float pacManSpeed;
	public float pacManDotsSpeed;
	public float ghostSpeed;
	public float ghostTunnelSpeed;
	public int elroy1DotsLeft;
	public float elroy1Speed;
	public int elroy2DotsLeft;
	public float elroy2Speed;
	public float pacManPowerSpeed;
	public float pacManPowerDotsSpeed;
	public float ghostFrightenedSpeed;
	public int pacManPowerSeconds;
	public int numFlashes;

	public int number;
	public int eatenFoodCount;
	public int ghostsKilledByEnergizer;
	public int ghostsKilled;

	public static GameLevel read(Object[] row) {
		GameLevel level = new GameLevel();
		if (row.length != 15) {
			throw new IllegalArgumentException(
					String.format("Level specification must contain 15 fields but has %d!", row.length));
		}
		int i = 0;
		level.bonusSymbol = Symbol.valueOf((String) row[i++]);
		level.bonusValue = integer(row[i++]);
		level.pacManSpeed = percentage(row[i++]);
		level.pacManDotsSpeed = percentage(row[i++]);
		level.ghostSpeed = percentage(row[i++]);
		level.ghostTunnelSpeed = percentage(row[i++]);
		level.elroy1DotsLeft = integer(row[i++]);
		level.elroy1Speed = percentage(row[i++]);
		level.elroy2DotsLeft = integer(row[i++]);
		level.elroy2Speed = percentage(row[i++]);
		level.pacManPowerSpeed = percentage(row[i++]);
		level.pacManPowerDotsSpeed = percentage(row[i++]);
		level.ghostFrightenedSpeed = percentage(row[i++]);
		level.pacManPowerSeconds = integer(row[i++]);
		level.numFlashes = integer(row[i++]);
		return level;
	}

	private static float percentage(Object value) {
		return ((int) value) / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}
}