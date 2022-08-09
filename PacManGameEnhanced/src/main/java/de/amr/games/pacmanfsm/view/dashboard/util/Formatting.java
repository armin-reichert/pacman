/*
MIT License

Copyright (c) 2019 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacmanfsm.view.dashboard.util;

import java.text.NumberFormat;
import java.util.Locale;

import de.amr.games.pacmanfsm.view.common.Rendering;
import de.amr.statemachine.core.InfiniteTimer;

public class Formatting {

	private Formatting() {
	}

	public static String integer(int i) {
		return NumberFormat.getNumberInstance(Locale.ENGLISH).format(i);
	}

	public static String percent(float f) {
		return NumberFormat.getPercentInstance(Locale.ENGLISH).format(f);
	}

	public static String ticksAndSeconds(long ticks) {
		if (ticks == InfiniteTimer.INFINITY) {
			return Rendering.INFTY;
		}
		float seconds = ticks / 60f;
		return seconds > 10 * 60 ? "> 10 min" : String.format("%d (%.2fs)", ticks, seconds);
	}

	public static String seconds(long ticks) {
		if (ticks == InfiniteTimer.INFINITY) {
			return Rendering.INFTY;
		}
		float seconds = ticks / 60f;
		return seconds > 10 * 60 ? "> 10 min" : String.format("%.2fs", ticks / 60f);
	}

	public static String pixelsPerSec(float speed) {
		return String.format("%.2f", speed * 60);
	}
}