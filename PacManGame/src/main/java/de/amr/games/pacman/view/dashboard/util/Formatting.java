package de.amr.games.pacman.view.dashboard.util;

import java.text.DecimalFormat;
import java.util.Locale;

public class Formatting {

	public static String integer(int i) {
		return DecimalFormat.getNumberInstance(Locale.ENGLISH).format(i);
	}

	public static String percent(float f) {
		return DecimalFormat.getPercentInstance(Locale.ENGLISH).format(f);
	}

	public static String ticksAndSeconds(int ticks) {
		return ticks == Integer.MAX_VALUE ? Character.toString('\u221E') : String.format("%d (%.2fs)", ticks, ticks / 60f);
	}

	public static String seconds(int ticks) {
		return ticks == Integer.MAX_VALUE ? Character.toString('\u221E') : String.format("%.2fs", ticks / 60f);
	}

	public static String pixelsPerSec(float speed) {
		return String.format("%.2f", speed * 60);
	}
}