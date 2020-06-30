package de.amr.games.pacman.model;

import de.amr.games.pacman.model.world.Symbol;

/**
 * Data structure storing the level-specific values.
 * 
 * @author Armin Reichert
 */
public class GameLevel {
	// constant values from table
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
	public int totalFoodCount;
	public int eatenFoodCount;
	public int ghostsKilledByEnergizer;
	public int ghostsKilled;

	public GameLevel(int n, int totalFoodCount, Object[] parameters) {

		this.number = n;
		this.totalFoodCount = totalFoodCount;
		this.eatenFoodCount = 0;
		this.ghostsKilledByEnergizer = 0;
		this.ghostsKilled = 0;

		int i = 0;
		bonusSymbol = Symbol.valueOf((String) parameters[i++]);
		bonusValue = integer(parameters[i++]);
		pacManSpeed = percentage(parameters[i++]);
		pacManDotsSpeed = percentage(parameters[i++]);
		ghostSpeed = percentage(parameters[i++]);
		ghostTunnelSpeed = percentage(parameters[i++]);
		elroy1DotsLeft = integer(parameters[i++]);
		elroy1Speed = percentage(parameters[i++]);
		elroy2DotsLeft = integer(parameters[i++]);
		elroy2Speed = percentage(parameters[i++]);
		pacManPowerSpeed = percentage(parameters[i++]);
		pacManPowerDotsSpeed = percentage(parameters[i++]);
		ghostFrightenedSpeed = percentage(parameters[i++]);
		pacManPowerSeconds = integer(parameters[i++]);
		numFlashes = integer(parameters[i++]);
	}

	public int remainingFoodCount() {
		return totalFoodCount - eatenFoodCount;
	}

	private static float percentage(Object value) {
		return ((int) value) / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}
}