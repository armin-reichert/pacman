package de.amr.games.pacman.view.theme.sound;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.SoundClip;
import de.amr.games.pacman.view.theme.api.PacManSounds;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public class ArcadePacManSounds implements PacManSounds {

	private static SoundClip mp3(String name) {
		return Assets.sound("sfx/" + name + ".mp3");
	}

	private SoundClip musicGameReady;
	private SoundClip musicGameRunning;
	private SoundClip musicGameOver;

	private CompletableFuture<Void> asyncLoader;

	@Override
	public void loadMusic() {
		asyncLoader = CompletableFuture.runAsync(() -> {
			musicGameRunning = mp3("bgmusic");
			musicGameOver = mp3("ending");
			musicGameReady = mp3("ready");
		});
	}

	@Override
	public Stream<SoundClip> clips() {
		return Stream.of(clipEating(), clipEatFruit(), clipEatGhost(), clipExtraLife(), clipGhostChase(), clipGhostDead(),
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
	public SoundClip clipEating() {
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
		return asyncLoader != null && asyncLoader.isDone();
	}

	@Override
	public void stopAllClips() {
		clips().forEach(SoundClip::stop);
	}

	@Override
	public void stopAll() {
		stopAllClips();
		musicGameReady().ifPresent(SoundClip::stop);
		musicGameRunning().ifPresent(SoundClip::stop);
		musicGameOver().ifPresent(SoundClip::stop);
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