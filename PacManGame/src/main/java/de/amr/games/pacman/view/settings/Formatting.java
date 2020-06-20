package de.amr.games.pacman.view.settings;

import java.text.DecimalFormat;
import java.util.Locale;

public class Formatting {
	public static String integer(int i) {
		return DecimalFormat.getNumberInstance(Locale.ENGLISH).format(i);
	}

	public static String percent(float f) {
		return DecimalFormat.getPercentInstance(Locale.ENGLISH).format(f);
	}

	public static String formatTicks(int ticks) {
		if (ticks == Integer.MAX_VALUE) {
			return Character.toString('\u221E');
		}
		return String.format("%d (%.2fs)", ticks, ticks / 60f);
	}

	public static String pixelsPerSec(float speed) {
		return String.format("%.2f", speed * 60);
	}

}
