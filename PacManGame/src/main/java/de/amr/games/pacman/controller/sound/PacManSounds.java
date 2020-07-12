package de.amr.games.pacman.controller.sound;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import de.amr.easy.game.assets.Assets;
import de.amr.easy.game.assets.SoundClip;
import de.amr.games.pacman.controller.creatures.ghost.GhostState;
import de.amr.games.pacman.controller.world.arcade.ArcadeWorldFolks;
import de.amr.games.pacman.model.world.api.World;

/**
 * Controls music and sound.
 * 
 * @author Armin Reichert
 */
public class PacManSounds {

	private final World world;
	private final ArcadeWorldFolks folks;
	private CompletableFuture<Void> musicLoading;
	private long lastPelletEatenTimeMillis;

	public PacManSounds(World world, ArcadeWorldFolks folks) {
		this.world = world;
		this.folks = folks;
	}

	private SoundClip mp3(String name) {
		return Assets.sound("sfx/" + name + ".mp3");
	}

	public void updatePlayingSounds() {
		if (snd_eatPill().isRunning() && System.currentTimeMillis() - lastPelletEatenTimeMillis > 250) {
			snd_eatPill().stop();
		}
		if (folks.ghosts().filter(world::contains).anyMatch(ghost -> ghost.is(GhostState.CHASING))) {
			if (!snd_ghost_chase().isRunning()) {
				snd_ghost_chase().loop();
			}
		} else {
			snd_ghost_chase().stop();
		}
		if (folks.ghosts().filter(world::contains).anyMatch(ghost -> ghost.is(GhostState.DEAD))) {
			if (!snd_ghost_dead().isRunning()) {
				snd_ghost_dead().loop();
			}
		} else {
			snd_ghost_dead().stop();
		}
	}

	public void loadMusic() {
		musicLoading = CompletableFuture.runAsync(() -> {
			music_playing();
			music_gameover();
		});
	}

	public boolean isMusicLoadingComplete() {
		return musicLoading != null && musicLoading.isDone();
	}

	public void stopAll() {
		stopAllClips();
		music_playing().stop();
		music_gameover().stop();
	}

	public void stopAllClips() {
		clips_all().forEach(SoundClip::stop);
	}

	public void stopGhostSounds() {
		snd_ghost_chase().stop();
		snd_ghost_dead().stop();
	}

	public void gameStarts() {
		music_playing().volume(.90f);
		music_playing().loop();
	}

	public void gameReady() {
		snd_ready().play();
	}

	public void pelletEaten() {
		if (!snd_eatPill().isRunning()) {
			snd_eatPill().loop();
		}
		lastPelletEatenTimeMillis = System.currentTimeMillis();
	}

	public void ghostEaten() {
		snd_eatGhost().play();
	}

	public void bonusEaten() {
		snd_eatFruit().play();
	}

	public void pacManLostPower() {
		snd_waza().stop();
	}

	public void pacManGainsPower() {
		if (!snd_waza().isRunning()) {
			snd_waza().loop();
		}
	}

	public void pacManDied() {
		snd_die().play();
		music_playing().stop();
	}

	public void resumePlayingMusic() {
		music_playing().loop();
	}

	public void extraLife() {
		snd_extraLife().play();
	}

	public void gameOver() {
		music_playing().stop();
		music_gameover().play();
	}

	public boolean isGameOverMusicRunning() {
		return music_gameover().isRunning();
	}

	public Stream<SoundClip> clips_all() {
		return Stream.of(snd_die(), snd_eatFruit(), snd_eatGhost(), snd_eatPill(), snd_extraLife(), snd_insertCoin(),
				snd_ready(), snd_ghost_chase(), snd_ghost_dead(), snd_waza());
	}

	public SoundClip music_playing() {
		return mp3("bgmusic");
	}

	public SoundClip music_gameover() {
		return mp3("ending");
	}

	public SoundClip snd_die() {
		return mp3("die");
	}

	public SoundClip snd_eatFruit() {
		return mp3("eat-fruit");
	}

	public SoundClip snd_eatGhost() {
		return mp3("eat-ghost");
	}

	public SoundClip snd_eatPill() {
		return mp3("eating");
	}

	public SoundClip snd_extraLife() {
		return mp3("extra-life");
	}

	public SoundClip snd_insertCoin() {
		return mp3("insert-coin");
	}

	public SoundClip snd_ready() {
		return mp3("ready");
	}

	public SoundClip snd_ghost_dead() {
		return mp3("ghost-dead");
	}

	public SoundClip snd_ghost_chase() {
		return mp3("ghost-chase");
	}

	public SoundClip snd_waza() {
		return mp3("waza");
	}
}
