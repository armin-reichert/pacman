package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.LOGGER;
import static de.amr.games.pacman.model.BonusSymbol.APPLE;
import static de.amr.games.pacman.model.BonusSymbol.BELL;
import static de.amr.games.pacman.model.BonusSymbol.CHERRIES;
import static de.amr.games.pacman.model.BonusSymbol.GALAXIAN;
import static de.amr.games.pacman.model.BonusSymbol.GRAPES;
import static de.amr.games.pacman.model.BonusSymbol.KEY;
import static de.amr.games.pacman.model.BonusSymbol.PEACH;
import static de.amr.games.pacman.model.BonusSymbol.STRAWBERRY;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href=
 *      "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 * @see <a href=
 *      "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Pac-Man
 *      level specifications</a>
 */
public class PacManGame {

	public static final Logger FSM_LOGGER = Logger.getLogger("StateMachineLogger");

	static {
		FSM_LOGGER.setLevel(Level.OFF);
	}

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOSTS[] = { 200, 400, 800, 1600 };

	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;

	public static final int SPEED_1_FPS = 60;
	public static final int SPEED_2_FPS = 70;
	public static final int SPEED_3_FPS = 80;

	final File hiscoreFile = new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml");

	final PacManGameLevel[] levels = new PacManGameLevel[] {
		/*@formatter:off*/
		null, // level numbering starts at 1
		new PacManGameLevel(CHERRIES,   100,  .80f, .71f, .75f, .40f,  20, .8f,  10,  .85f, .90f, .79f, .50f,   6, 5 ),
		new PacManGameLevel(STRAWBERRY, 300,  .90f, .79f, .85f, .45f,  30, .8f,  15,  .95f, .95f, .83f, .55f,   5, 5 ),
		new PacManGameLevel(PEACH,      500,  .90f, .79f, .85f, .45f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   4, 5 ),
		new PacManGameLevel(PEACH,      500,  .90f, .79f, .85f, .50f,  40, .8f,  20,  .95f, .95f, .83f, .55f,   3, 5 ),
		new PacManGameLevel(APPLE,      700,    1f, .87f, .95f, .50f,  40, .8f,  20, .105f,   1f, .87f, .60f,   2, 5 ),
		new PacManGameLevel(APPLE,      700,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   5, 5 ),
		new PacManGameLevel(GRAPES,    1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 ),
		new PacManGameLevel(GRAPES,    1000,    1f, .87f, .95f, .50f,  50, .8f,  25, .105f,   1f, .87f, .60f,   2, 5 ),
		new PacManGameLevel(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   1, 3 ),
		new PacManGameLevel(GALAXIAN,  2000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   5, 5 ),
		new PacManGameLevel(BELL,      3000,    1f, .87f, .95f, .50f,  60, .8f,  30, .105f,   1f, .87f, .60f,   2, 5 ),
		new PacManGameLevel(BELL,      3000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   1, 3 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f,  80, .8f,  40, .105f,   1f, .87f, .60f,   3, 5 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   1f, .87f, .60f,   1, 3 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 100, .8f,  50, .105f,   0f,   0f,   0f,   0, 0 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		new PacManGameLevel(KEY,       5000,    1f, .87f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		new PacManGameLevel(KEY,       5000,  .90f, .79f, .95f, .50f, 120, .8f,  60, .105f,   0f,   0f,   0f,   0, 0 ),
		/*@formatter:on*/
	};

	private final Maze maze = new Maze();
	private final Deque<BonusSymbol> levelSymbols = new ArrayDeque<>(7);
	private final Hiscore hiscore = new Hiscore();
	private PacManGameLevel level;
	public int lives;
	public int score;

	public void init() {
		lives = 3;
		score = 0;
		hiscore.load(hiscoreFile);
		levelSymbols.clear();
		enterLevel(1);
	}

	public void enterLevel(int n) {
		LOGGER.info(() -> "Enter level " + n);
		level = levels[Math.min(n, levels.length - 1)];
		level.number = n;
		level.numPelletsEaten = 0;
		level.ghostsKilledByEnergizer = 0;
		level.ghostKilledInLevel = 0;
		if (levelSymbols.size() == 7) {
			levelSymbols.removeLast();
		}
		levelSymbols.addFirst(level.bonusSymbol);
		maze.restoreFood();
		saveHiscore();
	}

	public Maze maze() {
		return maze;
	}

	public Hiscore hiscore() {
		return hiscore;
	}

	public PacManGameLevel level() {
		return level;
	}

	public Collection<BonusSymbol> levelSymbols() {
		return Collections.unmodifiableCollection(levelSymbols);
	}

	public int numPelletsRemaining() {
		return maze.totalNumPellets - level.numPelletsEaten;
	}

	/**
	 * @param tile tile containing food
	 * @return points scored
	 */
	public int eatFoodAt(Tile tile) {
		level.numPelletsEaten += 1;
		if (tile.containsEnergizer()) {
			level.ghostsKilledByEnergizer = 0;
			tile.removeFood();
			return POINTS_ENERGIZER;
		} else {
			tile.removeFood();
			return POINTS_PELLET;
		}
	}

	public boolean isBonusScoreReached() {
		return level.numPelletsEaten == 70 || level.numPelletsEaten == 170;
	}

	public void saveHiscore() {
		hiscore.save(hiscoreFile);
	}

	public void score(int points) {
		int oldScore = score;
		score += points;
		if (score > hiscore.points) {
			hiscore.points = score;
			hiscore.levelNumber = level.number;
		}
		if (oldScore < 10_000 && 10_000 <= score) {
			lives += 1;
		}
	}

	public void scoreKilledGhost(String ghostName) {
		int points = POINTS_GHOSTS[level.ghostsKilledByEnergizer];
		level.ghostsKilledByEnergizer += 1;
		level.ghostKilledInLevel += 1;
		score(points);
		if (level.ghostKilledInLevel == 16) {
			score(12000);
		}
		LOGGER.info(() -> String.format("Scored %d points for killing %s (%s ghost in sequence)", points, ghostName,
				new String[] { "", "first", "2nd", "3rd", "4th" }[level.ghostsKilledByEnergizer]));
	}
}