package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.util.ArrayList;
import java.util.List;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man
 *      dossier</a>
 */
public class PacManGame {

	/**
	 * <img src="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">
	 */
	private static List<?> levelData(int level) {
		if (level < 1) {
			throw new IllegalArgumentException("Illegal game level number: " + level);
		}
		switch (level) {
		/*@formatter:off*/
		case  1: return List.of("CHERRIES",   100,  80,  71,  75, 40,  20,  80, 10,  85,  90, 79, 50, 6, 5);
		case  2: return List.of("STRAWBERRY", 300,  90,  79,  85, 45,  30,  90, 15,  95,  95, 83, 55, 5, 5);
		case  3: return List.of("PEACH",      500,  90,  79,  85, 45,  40,  90, 20,  95,  95, 83, 55, 4, 5);
		case  4: return List.of("PEACH",      500,  90,  79,  85, 50,  40, 100, 20,  95,  95, 83, 55, 3, 5);
		case  5: return List.of("APPLE",      700, 100,  87,  95, 50,  40, 100, 20, 105, 100, 87, 60, 2, 5);
		case  6: return List.of("APPLE",      700, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 5, 5);
		case  7: return List.of("GRAPES",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5);
		case  8: return List.of("GRAPES",    1000, 100,  87,  95, 50,  50, 100, 25, 105, 100, 87, 60, 2, 5);
		case  9: return List.of("GALAXIAN",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 1, 3);
		case 10: return List.of("GALAXIAN",  2000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 5, 5);
		case 11: return List.of("BELL",      3000, 100,  87,  95, 50,  60, 100, 30, 105, 100, 87, 60, 2, 5);
		case 12: return List.of("BELL",      3000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3);
		case 13: return List.of("KEY",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 1, 3);
		case 14: return List.of("KEY",       5000, 100,  87,  95, 50,  80, 100, 40, 105, 100, 87, 60, 3, 5);
		case 15: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 1, 3);
		case 16: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,  0,  0, 1, 3);
		case 17: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105, 100, 87, 60, 0, 0);
		case 18: return List.of("KEY",       5000, 100,  87,  95, 50, 100, 100, 50, 105,   0,   0, 0, 1, 0);
		case 19: return List.of("KEY",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		case 20: return List.of("KEY",       5000, 100,  87,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		default: return List.of("KEY",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		/*@formatter:on*/
		}
	}

	private static float percent(Object value) {
		return (int) value / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}

	/*@formatter:off*/
	public static final int LIVES              = 3;
	public static final int POINTS_PELLET      = 10;
	public static final int POINTS_ENERGIZER   = 50;
	public static final int POINTS_EXTRA_LIFE  = 10_000;
	public static final int POINTS_ALL_GHOSTS  = 12_000;
	public static final int POINTS_BONUS[]     = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int POINTS_GHOSTS[]    = { 200, 400, 800, 1600 };
	public static final int FAT_PELLET         = 1;
	public static final int FAT_ENERGIZER      = 3;
	public static final int BONUS_ACTIVATION_1 = 70;
	public static final int BONUS_ACTIVATION_2 = 170;
	public static final int BONUS_SECONDS      = 9;
	/*@formatter:on*/

	public static PacManGame game;

	public static boolean started() {
		return game != null;
	}

	public static void startNewGame(int startLevel, int totalFoodCount) {
		game = new PacManGame(startLevel, totalFoodCount, LIVES, 0);
		game.hiscore = new Hiscore();
		game.hiscore.load();
		game.levelCounter = new ArrayList<>();
		game.levelCounter.add(game.bonusSymbol);
		loginfo("Game started at level %d", startLevel);
	}

	public void nextLevel() {
		PacManGame next = new PacManGame(game.level + 1, game.foodCount, game.lives, game.score);
		next.hiscore = game.hiscore;
		next.levelCounter = game.levelCounter;
		next.levelCounter.add(next.bonusSymbol);
		game = next;
		loginfo("Game level %d started", next.level);
	}

	//@formatter:off
	public final String bonusSymbol;
	public final int    bonusValue;
	public final float  pacManSpeed;
	public final float  pacManDotsSpeed;
	public final float  ghostSpeed;
	public final float  ghostTunnelSpeed;
	public final int    elroy1DotsLeft;
	public final float  elroy1Speed;
	public final int    elroy2DotsLeft;
	public final float  elroy2Speed;
	public final float  pacManPowerSpeed;
	public final float  pacManPowerDotsSpeed;
	public final float  ghostFrightenedSpeed;
	public final int    pacManPowerSeconds;
	public final int    numFlashes;
	//@formatter:on

	public final int level;
	public final int foodCount;

	public int eatenFoodCount;
	public int ghostsKilledByEnergizer;
	public int ghostsKilledInLevel;
	public int lives;
	public int score;

	public Hiscore hiscore;
	public List<String> levelCounter;

	private final ScoreResult scored = new ScoreResult(0, false);

	private PacManGame(int level, int foodCount, int lives, int score) {
		this.level = level;
		this.foodCount = foodCount;
		this.lives = lives;
		this.score = score;
		List<?> data = levelData(level);
		bonusSymbol = (String) data.get(0);
		bonusValue = integer(data.get(1));
		pacManSpeed = percent(data.get(2));
		pacManDotsSpeed = percent(data.get(3));
		ghostSpeed = percent(data.get(4));
		ghostTunnelSpeed = percent(data.get(5));
		elroy1DotsLeft = integer(data.get(6));
		elroy1Speed = percent(data.get(7));
		elroy2DotsLeft = integer(data.get(8));
		elroy2Speed = percent(data.get(9));
		pacManPowerSpeed = percent(data.get(10));
		pacManPowerDotsSpeed = percent(data.get(11));
		ghostFrightenedSpeed = percent(data.get(12));
		pacManPowerSeconds = integer(data.get(13));
		numFlashes = integer(data.get(14));
	}

	public int remainingFoodCount() {
		return foodCount - eatenFoodCount;
	}

	/**
	 * Scores the given number of points and handles high score and extra life.
	 * 
	 * @param points points to score
	 * @return score result
	 */
	private ScoreResult score(int points) {
		scored.points = points;
		scored.extraLife = score < POINTS_EXTRA_LIFE && score + points >= POINTS_EXTRA_LIFE;
		score += scored.points;
		lives += scored.extraLife ? 1 : 0;
		hiscore.check(level, score);
		return scored;
	}

	/**
	 * Scores an eaten bonus.
	 * 
	 * @return score result
	 */
	public ScoreResult scoreBonus() {
		return score(bonusValue);
	}

	/**
	 * Scores eating an energizer.
	 * 
	 * @return points scored
	 */
	public ScoreResult scoreEnergizerEaten() {
		eatenFoodCount += 1;
		ghostsKilledByEnergizer = 0;
		return score(POINTS_ENERGIZER);
	}

	/**
	 * Scores eating a simple pellet
	 * 
	 * @return points scored
	 */
	public ScoreResult scoreSimplePelletEaten() {
		eatenFoodCount += 1;
		return score(POINTS_PELLET);
	}

	/**
	 * Scores killing a ghost. Value of a killed ghost doubles if killed in series using the same
	 * energizer.
	 */
	public ScoreResult scoreGhostKilled() {
		ghostsKilledByEnergizer += 1;
		ghostsKilledInLevel += 1;
		int ghostBounty = ghostBounty();
		loginfo("Got %d points for killing %s ghost", ghostBounty,
				new String[] { "", "first", "2nd", "3rd", "4th" }[ghostsKilledByEnergizer]);
		int points = ghostBounty;
		if (ghostsKilledInLevel == 16) {
			points += POINTS_ALL_GHOSTS;
		}
		return score(points);
	}

	/**
	 * @return value of killed ghost (doubles for each ghost killed by the same energizer)
	 */
	public int ghostBounty() {
		return POINTS_GHOSTS[ghostsKilledByEnergizer > 0 ? ghostsKilledByEnergizer - 1 : 0];
	}

	/**
	 * @return {@code true} if the number of eaten pellets causes the bonus to get active
	 */
	public boolean isBonusDue() {
		return eatenFoodCount == BONUS_ACTIVATION_1 || eatenFoodCount == BONUS_ACTIVATION_2;
	}
}