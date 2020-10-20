package de.amr.games.pacman;

import java.util.List;

/**
 * The level-specific data.
 * <ol start="0">
 * <li>Bonus Symbol
 * <li>Bonus Points
 * <li>Pac-Man Speed
 * <li>Pac-Man Dots Speed,
 * <li>Ghost Speed
 * <li>Ghost Tunnel Speed
 * <li>Elroy1 Dots Left
 * <li>Elroy1 Speed
 * <li>Elroy2 Dots Left
 * <li>Elroy2 Speed,
 * <li>Frightening Pac-Man Speed
 * <li>Frightening Pac-Man Dots Speed
 * <li>Frightened Ghost Speed,
 * <li>Frightened Time (sec)
 * <li>Number of Flashes.
 * </ol>
 * <img src="../../../../../resources/levels.png">
 */
public class LevelData {

	private List<?> values;

	public static LevelData of(Object... values) {
		LevelData data = new LevelData();
		data.values = List.of(values);
		return data;
	}

	public String bonusSymbol() {
		return stringValue(0);
	}

	public int bonusPoints() {
		return intValue(1);
	}

	public float pacManSpeed() {
		return percentValue(2);
	}

	public float pacManDotsSpeed() {
		return percentValue(3);
	}

	public float ghostSpeed() {
		return percentValue(4);
	}

	public float ghostTunnelSpeed() {
		return percentValue(5);
	}

	public int elroy1DotsLeft() {
		return intValue(6);
	}

	public float elroy1Speed() {
		return percentValue(7);
	}

	public int elroy2DotsLeft() {
		return intValue(8);
	}

	public float elroy2Speed() {
		return percentValue(9);
	}

	public float pacManPowerSpeed() {
		return percentValue(10);
	}

	public float pacManPowerDotsSpeed() {
		return percentValue(11);
	}

	public float frightenedGhostSpeed() {
		return percentValue(12);
	}

	public int ghostFrightenedSeconds() {
		return intValue(13);
	}

	public int numFlashes() {
		return intValue(14);
	}

	private float percentValue(int index) {
		return intValue(index) / 100f;
	}

	private int intValue(int index) {
		return (int) values.get(index);
	}

	private String stringValue(int index) {
		return (String) values.get(index);
	}
}