package de.amr.games.pacman.controller.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import de.amr.games.pacman.view.api.Theme;

public class ThemeSelector {

	private int current;
	private Theme[] themes;
	private List<Consumer<Theme>> listeners = new ArrayList<>();

	public ThemeSelector(Stream<Theme> themes) {
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