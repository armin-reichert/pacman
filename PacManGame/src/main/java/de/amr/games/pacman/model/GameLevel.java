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

	public GameLevel(Symbol bonusSymbol, int bonusValue, float pacManSpeed, float pacManDotsSpeed, float ghostSpeed,
			float ghostTunnelSpeed, int elroy1DotsLeft, float elroy1Speed, int elroy2DotsLeft, float elroy2Speed,
			float pacManPowerSpeed, float pacManPowerDotsSpeed, float ghostFrightenedSpeed, int pacManPowerSeconds,
			int numFlashes) {
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
		this.numFlashes = numFlashes;
	}

	public GameLevel(String levelSpec) {
		List<String> fields = Arrays.stream(levelSpec.split(",")).map(String::strip).collect(toList());
		for (Iterator<String> i = fields.iterator(); i.hasNext();) {
			bonusSymbol = Symbol.valueOf(i.next());
			bonusValue = intValue(i.next());
			pacManSpeed = percentValue(i.next());
			pacManDotsSpeed = percentValue(i.next());
			ghostSpeed = percentValue(i.next());
			ghostTunnelSpeed = percentValue(i.next());
			elroy1DotsLeft = intValue(i.next());
			elroy1Speed = percentValue(i.next());
			elroy2DotsLeft = intValue(i.next());
			elroy2Speed = percentValue(i.next());
			pacManPowerSpeed = percentValue(i.next());
			pacManPowerDotsSpeed = percentValue(i.next());
			ghostFrightenedSpeed = percentValue(i.next());
			pacManPowerSeconds = intValue(i.next());
			numFlashes = intValue(i.next());
		}
	}

	int intValue(String s) {
		try {
			return DecimalFormat.getNumberInstance(Locale.ENGLISH).parse(s).intValue();
		} catch (ParseException x) {
			throw new RuntimeException(x);
		}
	}

	float percentValue(String s) {
		try {
			return DecimalFormat.getPercentInstance(Locale.ENGLISH).parse(s).floatValue();
		} catch (ParseException x) {
			throw new RuntimeException(x);
		}
	}
}