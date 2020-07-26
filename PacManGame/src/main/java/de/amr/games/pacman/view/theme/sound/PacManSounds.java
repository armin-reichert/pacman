package de.amr.games.pacman.view.theme.sound;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.SoundClip;
import de.amr.games.pacman.controller.creatures.Folks;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.view.theme.api.IPacManSounds;

/**
 * Clips and music.
 * 
 * @author Armin Reichert
 */
public class PacManSounds implements IPacManSounds {

	private static SoundClip mp3(String name) {
		return Assets.sound("sfx/" + name + ".mp3");
	}

	private final Folks folks;

	private final SoundClip clipEating = mp3("eating");
	private final SoundClip clipEatFruit = mp3("eat-fruit");
	private final SoundClip clipEatGhost = mp3("eat-ghost");
	private final SoundClip clipExtraLife = mp3("extra-life");
	private final SoundClip clipGhostChase = mp3("ghost-chase");
	private final SoundClip clipGhostDead = mp3("ghost-dead");
	private final SoundClip clipInsertCoin = mp3("insert-coin");
	private final SoundClip clipPacmanDies = mp3("die");
	private final SoundClip clipWaza = mp3("waza");

	private Optional<SoundClip> musicGameReady = Optional.empty();
	private Optional<SoundClip> musicGameRunning = Optional.empty();
	private Optional<SoundClip> musicGameOver = Optional.empty();

	private CompletableFuture<Void> asyncLoader;

	private long lastPelletEatenTimeMillis;

	public PacManSounds(Folks folks) {
		this.folks = folks;
	}

	@Override
	public void loadMusicAsync() {
		asyncLoader = CompletableFuture.runAsync(() -> {
			musicGameRunning = Optional.of(mp3("bgmusic"));
			musicGameOver = Optional.of(mp3("ending"));
			musicGameReady = Optional.of(mp3("ready"));
		});
	}

	public Stream<SoundClip> clips() {
		return Stream.of(clipEating, clipEatFruit, clipEatGhost, clipExtraLife, clipGhostChase, clipGhostDead,
				clipInsertCoin, clipPacmanDies, clipWaza);
	}

	@Override
	public void updateRunningClips() {
		if (clipEating.isRunning() && System.currentTimeMillis() - lastPelletEatenTimeMillis > 250) {
			clipEating.stop();
		}
		if (folks.ghostsInWorld().anyMatch(ghost -> ghost.is(GhostState.CHASING))) {
			if (!clipGhostChase.isRunning()) {
				clipGhostChase.loop();
			}
		} else {
			clipGhostChase.stop();
		}
		if (folks.ghostsInWorld().anyMatch(ghost -> ghost.is(GhostState.DEAD))) {
			if (!clipGhostDead.isRunning()) {
				clipGhostDead.loop();
			}
		} else {
			clipGhostDead.stop();
		}
	}

	@Override
	public boolean isMusicLoadingComplete() {
		return asyncLoader != null && asyncLoader.isDone();
	}

	@Override
	public void stopAllClips() {
		clips().forEach(SoundClip::stop);
	}

	@Override
	public void stopAll() {
		stopAllClips();
		musicGameReady.ifPresent(SoundClip::stop);
		musicGameRunning.ifPresent(SoundClip::stop);
		musicGameOver.ifPresent(SoundClip::stop);
	}

	@Override
	public void playEatingPelletsSound() {
		if (!clipEating.isRunning()) {
			clipEating.loop();
		}
		lastPelletEatenTimeMillis = System.currentTimeMillis();
	}

	@Override
	public void stopEatingPelletsSound() {
		clipEating.stop();
	}

	@Override
	public void playClipEatBonus() {
		clipEatFruit.play();
	}

	@Override
	public void playClipEatGhost() {
		clipEatGhost.play();
	}

	@Override
	public void playClipExtraLife() {
		clipExtraLife.play();
	}

	@Override
	public void playClipGhostChasing() {
		clipGhostChase.play();
	}

	@Override
	public void loopClipGhostChasing() {
		clipGhostChase.loop();
	}

	@Override
	public void stopClipGhostChasing() {
		clipGhostChase.stop();
	}

	@Override
	public void playClipInsertCoin() {
		clipInsertCoin.play();
	}

	@Override
	public void playClipPacManLostPower() {
		clipWaza.stop();
	}

	@Override
	public void playClipPacManGainsPower() {
		if (!clipWaza.isRunning()) {
			clipWaza.loop();
		}
	}

	@Override
	public void playClipPacManDies() {
		clipPacmanDies.play();
	}

	@Override
	public void playMusicGameOver() {
		musicGameOver.ifPresent(SoundClip::play);
	}

	@Override
	public void playMusicGameReady() {
		musicGameReady.ifPresent(SoundClip::play);
	}

	@Override
	public void playMusicGameRunning() {
		musicGameRunning.ifPresent(SoundClip::loop);
	}

	@Override
	public void stopMusicGameRunning() {
		musicGameRunning.ifPresent(SoundClip::stop);
	}

	@Override
	public boolean isGameOverMusicRunning() {
		return musicGameOver.map(SoundClip::isRunning).orElse(false);
	}
}