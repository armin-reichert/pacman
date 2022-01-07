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
package de.amr.games.pacman.controller.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import de.amr.games.pacman.theme.api.Theme;

public class ThemeSelector {

	private int current;
	private Theme[] themes;
	private List<Consumer<Theme>> listeners = new ArrayList<>();

	public ThemeSelector(List<Theme> themes) {
		this.themes = themes.toArray(Theme[]::new);
		if (this.themes.length == 0) {
			throw new IllegalArgumentException("At least one theme must be provided");
		}
	}

	public void addListener(Consumer<Theme> listener) {
		listeners.add(listener);
	}

	public Theme current() {
		return themes[current];
	}

	public Theme next() {
		current += 1;
		if (current == themes.length) {
			current = 0;
		}
		listeners.forEach(l -> l.accept(current()));
		return current();
	}

	public Theme prev() {
		current -= 1;
		if (current == -1) {
			current = themes.length - 1;
		}
		listeners.forEach(l -> l.accept(current()));
		return current();
	}

	public Theme select(String themeName) {
		//@formatter:off
		current = IntStream.range(0, themes.length)
			.filter(i -> themes[i].name().equalsIgnoreCase(themeName))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Illegal theme name: " + themeName));
		//@formatter:on
		listeners.forEach(l -> l.accept(current()));
		return current();
	}
}