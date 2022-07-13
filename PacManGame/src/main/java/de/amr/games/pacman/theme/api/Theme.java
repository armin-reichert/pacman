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
package de.amr.games.pacman.theme.api;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import de.amr.games.pacman.view.api.PacManGameSounds;

/**
 * Interface implemented by every theme.
 * 
 * @author Armin Reichert
 */
public interface Theme {

	String name();

	int asInt(String key);

	float asFloat(String key);

	Font asFont(String key);

	Color asColor(String key);

	Image asIimage(String key);

	<T> T asValue(String key);

	WorldRenderer worldRenderer();

	PacManRenderer pacManRenderer();

	GhostRenderer ghostRenderer();

	GameRenderer levelCounterRenderer();

	GameRenderer livesCounterRenderer();

	GameRenderer gameScoreRenderer();

	MessagesRenderer messagesRenderer();

	PacManGameSounds sounds();
}