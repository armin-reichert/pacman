package de.amr.games.pacman.view.dashboard.util;

import java.text.DecimalFormat;
import java.util.Locale;

import de.amr.games.pacman.view.common.Rendering;
import de.amr.statemachine.core.InfiniteTimer;

public class Formatting {

	public static String integer(int i) {
		return DecimalFormat.getNumberInstance(Locale.ENGLISH).format(i);
	}

	public static String percent(float f) {
		return DecimalFormat.getPercentInstance(Locale.ENGLISH).format(f);
	}

	public static String ticksAndSeconds(long ticks) {
		float seconds = ticks / 60f;
		return ticks == InfiniteTimer.INFINITY ? Rendering.INFTY
				: seconds > 10 * 60 ? "> 10 min" : String.format("%d (%.2fs)", ticks, seconds);
	}

	public static String seconds(long ticks) {
		float seconds = ticks / 60f;
		return ticks == InfiniteTimer.INFINITY ? Rendering.INFTY
				: seconds > 10 * 60 ? "> 10 min" : String.format("%.2fs", ticks / 60f);
	}

	public static String pixelsPerSec(float speed) {
		return String.format("%.2f", speed * 60);
	}
}