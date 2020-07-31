package de.amr.games.pacman.view.theme.api;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.easy.game.assets.SoundClip;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public interface PacManSounds {

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