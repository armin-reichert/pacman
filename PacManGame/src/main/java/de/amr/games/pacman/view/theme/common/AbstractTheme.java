package de.amr.games.pacman.view.theme.common;

import de.amr.games.pacman.view.theme.api.Theme;

public abstract class AbstractTheme extends ParameterMap implements Theme {

	protected final String name;

	public AbstractTheme(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}
}
