package de.amr.games.pacman.view.theme.arcade.sounds;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.SoundClip;
import de.amr.games.pacman.view.api.PacManSounds;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public class ArcadeSounds implements PacManSounds {

	public static final ArcadeSounds SOUNDS = new ArcadeSounds();

	private static SoundClip mp3(String name) {
		return Assets.sound("themes/arcade/sounds/" + name + ".mp3");
	}

	private SoundClip musicGameReady, musicGameRunning, musicGameOver;

	@Override
	public Stream<SoundClip> clips() {
		return Stream.of(clipCrunching(), clipEatFruit(), clipEatGhost(), clipExtraLife(), clipGhostChase(), clipGhostDead(),
				clipInsertCoin(), clipPacManDies(), clipWaza());
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
		return musicGameReady().isPresent() && musicGameRunning().isPresent() && musicGameOver().isPresent();
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