package de.amr.games.pacman.model;

import static java.util.stream.Collectors.toList;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

	public static GameLevel parse(String levelSpec) throws ParseException {
		GameLevel level = new GameLevel();
		List<String> fields = Arrays.stream(levelSpec.trim().split(",")).map(String::strip).collect(toList());
		if (fields.size() != 15) {
			throw new IllegalArgumentException(
					String.format("Level specification must contain 15 fields but has %d!", fields.size()));
		}
		Iterator<String> f = fields.iterator();
		level.bonusSymbol = Symbol.valueOf(f.next());
		level.bonusValue = integer(f.next());
		level.pacManSpeed = percent(f.next());
		level.pacManDotsSpeed = percent(f.next());
		level.ghostSpeed = percent(f.next());
		level.ghostTunnelSpeed = percent(f.next());
		level.elroy1DotsLeft = integer(f.next());
		level.elroy1Speed = percent(f.next());
		level.elroy2DotsLeft = integer(f.next());
		level.elroy2Speed = percent(f.next());
		level.pacManPowerSpeed = percent(f.next());
		level.pacManPowerDotsSpeed = percent(f.next());
		level.ghostFrightenedSpeed = percent(f.next());
		level.pacManPowerSeconds = integer(f.next());
		level.numFlashes = integer(f.next());
		return level;
	}

	static int integer(String s) throws ParseException {
		return DecimalFormat.getNumberInstance(Locale.ENGLISH).parse(s).intValue();
	}

	static float percent(String s) throws ParseException {
		return DecimalFormat.getPercentInstance(Locale.ENGLISH).parse(s).floatValue();
	}
}