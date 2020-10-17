package de.amr.games.pacman;

import java.util.List;

public class LevelData {

	private List<?> values;

	public static LevelData of(Object... values) {
		LevelData data = new LevelData();
		data.values = List.of(values);
		return data;
	}

	public float percentValue(int index) {
		return intValue(index) / 100f;
	}

	public int intValue(int index) {
		return (int) values.get(index);
	}

	public String stringValue(int index) {
		return (String) values.get(index);
	}
}