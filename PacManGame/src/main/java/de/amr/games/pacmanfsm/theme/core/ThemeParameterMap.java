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
package de.amr.games.pacmanfsm.theme.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

public class ThemeParameterMap {

	protected final Map<String, Object> parameters = new HashMap<>();

	public void set(String key, Object value) {
		parameters.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public <T> T asValue(String key) {
		return (T) parameters.get(key);
	}

	public int asInt(String key) {
		return (int) parameters.getOrDefault(key, 0);
	}

	public float asFloat(String key) {
		return (float) parameters.getOrDefault(key, 0f);
	}

	public Color asColor(String key) {
		return (Color) parameters.getOrDefault(key, Color.BLACK);
	}

	public Font asFont(String key) {
		return (Font) parameters.get(key);
	}

	public Image asIimage(String key) {
		return (Image) parameters.get(key);
	}
}