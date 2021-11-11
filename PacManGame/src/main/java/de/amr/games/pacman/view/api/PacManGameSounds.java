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
package de.amr.games.pacman.view.api;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.assets.SoundClip;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public interface PacManGameSounds {

	SoundClip clipCrunching();

	SoundClip clipEatFruit();

	SoundClip clipEatGhost();

	SoundClip clipExtraLife();

	SoundClip clipGhostChase();

	SoundClip clipGhostDead();

	SoundClip clipInsertCoin();

	SoundClip clipPacManDies();

	SoundClip clipWaza();

	Stream<SoundClip> clips();

	void loadMusic();

	boolean isMusicLoaded();

	Stream<SoundClip> loadedMusic();

	Optional<SoundClip> musicGameReady();

	Optional<SoundClip> musicGameRunning();

	Optional<SoundClip> musicGameOver();

	default void playMusic(Optional<SoundClip> music) {
		music.ifPresent(SoundClip::play);
	}

	default void stopMusic(Optional<SoundClip> music) {
		music.ifPresent(SoundClip::stop);
	}

	default boolean isMusicRunning(Optional<SoundClip> music) {
		return music.map(SoundClip::isRunning).orElse(false);
	}

	default void stopAll() {
		clips().forEach(SoundClip::stop);
		loadedMusic().forEach(SoundClip::stop);
	}
}