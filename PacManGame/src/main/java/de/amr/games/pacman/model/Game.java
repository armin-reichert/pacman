package de.amr.games.pacman.model;

import static de.amr.easy.game.Application.loginfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.amr.games.pacman.model.world.PacManWorld;
import de.amr.games.pacman.model.world.Symbol;
import de.amr.games.pacman.model.world.Tile;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 * @see <a href= "http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">Pac-Man level
 *      specifications</a>
 */
public class Game {

	/**
	 * <img src="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">
	 */
	public static final String[] LEVELS = {
		/*@formatter:off*/
		"CHERRIES,   100,  80%,  71%,  75%,  40%,  20,  80%,  10,  85%,  90%, 79%, 50%, 6, 5",
		"STRAWBERRY, 300,  90%,  79%,  85%,  45%,  30,  90%,  15,  95%,  95%, 83%, 55%, 5, 5",
		"PEACH,      500,  90%,  79%,  85%,  45%,  40,  90%,  20,  95%,  95%, 83%, 55%, 4, 5",
		"PEACH,      500,  90%,  79%,  85%,  50%,  40, 100%,  20,  95%,  95%, 83%, 55%, 3, 5",
		"APPLE,      700, 100%,  87%,  95%,  50%,  40, 100%,  20, 105%, 100%, 87%, 60%, 2, 5",
		"APPLE,      700, 100%,  87%,  95%,  50%,  50, 100%,  25, 105%, 100%, 87%, 60%, 5, 5",
		"GRAPES,    1000, 100%,  87%,  95%,  50%,  50, 100%,  25, 105%, 100%, 87%, 60%, 2, 5",
		"GRAPES,    1000, 100%,  87%,  95%,  50%,  50, 100%,  25, 105%, 100%, 87%, 60%, 2, 5",
		"GALAXIAN,  2000, 100%,  87%,  95%,  50%,  60, 100%,  30, 105%, 100%, 87%, 60%, 1, 3",
		"GALAXIAN,  2000, 100%,  87%,  95%,  50%,  60, 100%,  30, 105%, 100%, 87%, 60%, 5, 5",
		"BELL,      3000, 100%,  87%,  95%,  50%,  60, 100%,  30, 105%, 100%, 87%, 60%, 2, 5",
		"BELL,      3000, 100%,  87%,  95%,  50%,  80, 100%,  40, 105%, 100%, 87%, 60%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%,  80, 100%,  40, 105%, 100%, 87%, 60%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%,  80, 100%,  40, 105%, 100%, 87%, 60%, 3, 5",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%, 100%, 87%, 60%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%,   0%,  0%,  0%, 1, 3",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%, 100%, 87%, 60%, 0, 0",
		"KEY,       5000, 100%,  87%,  95%,  50%, 100, 100%,  50, 105%,   0%,   0%, 0%, 1, 0",
		"KEY,       5000, 100%,  87%,  95%,  50%, 120, 100%,  60, 105%,   0%,   0%, 0%, 0, 0",
		"KEY,       5000, 100%,  87%,  95%,  50%, 120, 100%,  60, 105%,   0%,   0%, 0%, 0, 0",
		"KEY,       5000,  90%,  79%,  95%,  50%, 120, 100%,  60, 105%,   0%,   0%, 0%, 0, 0",
		/*@formatter:on*/
	};

	public static final int POINTS_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_EXTRA_LIFE = 10_000;
	public static final int POINTS_KILLED_ALL_GHOSTS = 12_000;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOST[] = { 200, 400, 800, 1600 };
	public static final int DIGEST_PELLET_TICKS = 1;
	public static final int DIGEST_ENERGIZER_TICKS = 3;
	public static final int BONUS_ACTIVATION[] = { 70, 170 };

	/**
	 * Returns the number of ticks corresponding to the given time (in seconds) for a framerate of 60
	 * ticks/sec.
	 * 
	 * @param seconds seconds
	 * @return ticks corresponding to given number of seconds
	 */
	public static int sec(float seconds) {
		return Math.round(60 * seconds);
	}

	private final PacManWorld world;
	public final List<Symbol> levelCounter;
	public final GameScore gameScore;
	public GameLevel level;
	public int lives;
	public int score;
	public int totalFoodCount;

	/**
	 * Creates a game starting with the given level.
	 * 
	 * @param startLevel start level number (1-...)
	 */
	public Game(PacManWorld world, int startLevel) {
		this.world = world;
		world.pacMan().game = this;
		world.ghosts().forEach(ghost -> ghost.game = this);
		levelCounter = new ArrayList<>();
		gameScore = new GameScore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		lives = 3;
		score = 0;
		enterLevel(startLevel);
	}

	/**
	 * Enters level with given number (starting at 1).
	 * 
	 * @param n level number (1-...)
	 */
	public void enterLevel(int n) {
		loginfo("Enter level %d", n);
		level = level(n);
		levelCounter.add(level.bonusSymbol);
		world.createFood();
		gameScore.load();
	}

	private GameLevel level(int n) {
		if (n < 1) {
			throw new IllegalArgumentException("Level numbering starts at 1");
		}
		if (n > LEVELS.length) {
			n = LEVELS.length;
		}
		try {
			GameLevel level = GameLevel.parse(LEVELS[n - 1]);
			level.number = n;
			return level;
		} catch (Exception x) {
			loginfo("ERROR: Data for level %d are invalid!", n);
			loginfo("%s", LEVELS[n - 1]);
			loginfo(x.getMessage());
			System.exit(0);
			return null;
		}
	}

	/**
	 * @return number of remaining pellets and energizers
	 */
	public int remainingFoodCount() {
		return totalFoodCount - level.eatenFoodCount;
	}

	/**
	 * Eats the pellet or eenrgizer on the given tile.
	 * 
	 * @param tile      tile containing food
	 * @param energizer tells if the pellet is an energizer
	 * @return points scored
	 */
	public int eatFood(Tile tile, boolean energizer) {
		if (!world.containsFood(tile)) {
			loginfo("Tile %s does not contain food", tile);
			return 0;
		}
		world.eatFood(tile);
		level.eatenFoodCount += 1;
		if (energizer) {
			level.ghostsKilledByEnergizer = 0;
			return POINTS_ENERGIZER;
		} else {
			return POINTS_PELLET;
		}
	}

	/**
	 * @return {@code true} if the number of eaten pellets causes the bonus to get active
	 */
	public boolean isBonusDue() {
		return level.eatenFoodCount == BONUS_ACTIVATION[0] || level.eatenFoodCount == BONUS_ACTIVATION[1];
	}

	/**
	 * Score the given number of points and handles high score, extra life etc.
	 * 
	 * @param points scored points
	 */
	public void score(int points) {
		int oldScore = score;
		score += points;
		gameScore.update(level, score);
		if (oldScore < POINTS_EXTRA_LIFE && POINTS_EXTRA_LIFE <= score) {
			lives += 1;
		}
	}

	/**
	 * Scores for killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 * 
	 * @param ghostName killed ghost's name
	 */
	public void scoreGhostKilled(String ghostName) {
		level.ghostsKilledByEnergizer += 1;
		level.ghostsKilled += 1;
		if (level.ghostsKilled == 16) {
			score(POINTS_KILLED_ALL_GHOSTS);
		}
		int points = killedGhostPoints();
		score(points);
		loginfo("Scored %d points for killing %s (%s ghost in sequence)", points, ghostName,
				new String[] { "", "first", "2nd", "3rd", "4th" }[level.ghostsKilledByEnergizer]);
	}

	/**
	 * @return current value of a killed ghost. Value doubles for each ghost killed by the same
	 *         energizer.
	 */
	public int killedGhostPoints() {
		return POINTS_GHOST[level.ghostsKilledByEnergizer - 1];
	}
}