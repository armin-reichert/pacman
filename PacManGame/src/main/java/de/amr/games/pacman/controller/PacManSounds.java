package de.amr.games.pacman.controller;

import java.util.concurrent.CompletableFuture;

import de.amr.easy.game.assets.SoundClip;
import de.amr.games.pacman.controller.actor.GhostState;
import de.amr.games.pacman.model.Game;
import de.amr.games.pacman.view.theme.Theme;

/**
 * Controls music and sound.
 * 
 * @author Armin Reichert
 */
public class PacManSounds {

	private Theme theme;
	private CompletableFuture<Void> musicLoading;
	private long lastPelletEatenTimeMillis;

	public PacManSounds(Theme theme) {
		this.theme = theme;
	}

	public void updatePlayingSounds(Game game) {
		if (theme.snd_eatPill().isRunning() && System.currentTimeMillis() - lastPelletEatenTimeMillis > 250) {
			theme.snd_eatPill().stop();
		}
		if (game.ghostsOnStage().anyMatch(ghost -> ghost.is(GhostState.CHASING))) {
			if (!theme.snd_ghost_chase().isRunning()) {
				theme.snd_ghost_chase().loop();
			}
		} else {
			theme.snd_ghost_chase().stop();
		}
		if (game.ghostsOnStage().anyMatch(ghost -> ghost.is(GhostState.DEAD))) {
			if (!theme.snd_ghost_dead().isRunning()) {
				theme.snd_ghost_dead().loop();
			}
		} else {
			theme.snd_ghost_dead().stop();
		}
	}

	public void loadMusic() {
		musicLoading = CompletableFuture.runAsync(() -> {
			theme.music_playing();
			theme.music_gameover();
		});
	}

	public boolean isMusicLoadingComplete() {
		return musicLoading != null && musicLoading.isDone();
	}

	public void stopAll() {
		stopAllClips();
		theme.music_playing().stop();
		theme.music_gameover().stop();
	}

	public void stopAllClips() {
		theme.clips_all().forEach(SoundClip::stop);
	}

	public void stopGhostSounds() {
		theme.snd_ghost_chase().stop();
		theme.snd_ghost_dead().stop();
	}

	public void gameStarts() {
		theme.music_playing().volume(.90f);
		theme.music_playing().loop();
	}

	public void gameReady() {
		theme.snd_ready().play();
	}

	public void pelletEaten() {
		if (!theme.snd_eatPill().isRunning()) {
			theme.snd_eatPill().loop();
		}
		lastPelletEatenTimeMillis = System.currentTimeMillis();
	}

	public void ghostEaten() {
		theme.snd_eatGhost().play();
	}

	public void bonusEaten() {
		theme.snd_eatFruit().play();
	}

	public void pacManLostPower() {
		theme.snd_waza().stop();
	}

	public void pacManGainsPower() {
		if (!theme.snd_waza().isRunning()) {
			theme.snd_waza().loop();
		}
	}

	public void pacManDied() {
		theme.snd_die().play();
		theme.music_playing().stop();
	}

	public void resumePlayingMusic() {
		theme.music_playing().start();
	}

	public void extraLife() {
		theme.snd_extraLife().play();
	}

	public void gameOver() {
		theme.music_playing().stop();
		theme.music_gameover().play();
	}

	public boolean isGameOverMusicRunning() {
		return theme.music_gameover().isRunning();
	}
}
