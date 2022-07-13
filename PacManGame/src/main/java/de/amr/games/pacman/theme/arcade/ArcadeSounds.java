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
package de.amr.games.pacman.theme.arcade;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.SoundClip;
import de.amr.games.pacman.view.api.PacManGameSounds;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public class ArcadeSounds implements PacManGameSounds {

	public static final ArcadeSounds SOUNDS = new ArcadeSounds();

	private static SoundClip mp3(String name) {
		return Assets.sound("themes/arcade/sounds/" + name + ".mp3");
	}

	private SoundClip musicGameReady;
	private SoundClip musicGameRunning;
	private SoundClip musicGameOver;

	@Override
	public Stream<SoundClip> clips() {
		return Stream.of(clipCrunching(), clipEatFruit(), clipEatGhost(), clipExtraLife(), clipGhostChase(),
				clipGhostDead(), clipInsertCoin(), clipPacManDies(), clipWaza());
	}

	@Override
	public SoundClip clipEatFruit() {
		return mp3("eat-fruit");
	}

	@Override
	public SoundClip clipEatGhost() {
		return mp3("eat-ghost");
	}

	@Override
	public SoundClip clipCrunching() {
		return mp3("eating");
	}

	@Override
	public SoundClip clipExtraLife() {
		return mp3("extra-life");
	}

	@Override
	public SoundClip clipGhostChase() {
		return mp3("ghost-chase");
	}

	@Override
	public SoundClip clipGhostDead() {
		return mp3("ghost-dead");
	}

	@Override
	public SoundClip clipInsertCoin() {
		return mp3("insert-coin");
	}

	@Override
	public SoundClip clipPacManDies() {
		return mp3("die");
	}

	@Override
	public SoundClip clipWaza() {
		return mp3("waza");
	}

	@Override
	public boolean isMusicLoaded() {
		return musicGameReady().isPresent();
//				&& musicGameRunning().isPresent() && musicGameOver().isPresent();
	}

	@Override
	public void loadMusic() {
		CompletableFuture.runAsync(() -> {
			musicGameReady = mp3("ready");
			musicGameRunning = mp3("bgmusic");
			musicGameOver = mp3("ending");
		});
	}

	@Override
	public Stream<SoundClip> loadedMusic() {
		return Stream.of(musicGameReady, musicGameRunning, musicGameOver).filter(Objects::nonNull);
	}

	@Override
	public Optional<SoundClip> musicGameOver() {
		return Optional.ofNullable(musicGameOver);
	}

	@Override
	public Optional<SoundClip> musicGameReady() {
		return Optional.ofNullable(musicGameReady);
	}

	@Override
	public Optional<SoundClip> musicGameRunning() {
		return Optional.ofNullable(musicGameRunning);
	}
}