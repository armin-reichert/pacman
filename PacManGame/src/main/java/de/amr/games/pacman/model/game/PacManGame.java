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
package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The "model" (in MVC speak) of the Pac-Man game.
 * 
 * @author Armin Reichert
 * 
 * @see <a href= "http://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php">Pac-Man dossier</a>
 */
public class PacManGame {

	public static PacManGame game;

	static final int PACMAN_LIVES = 3;
	static final int POINTS_PELLET = 10;
	static final int POINTS_ENERGIZER = 50;
	static final int POINTS_EXTRA_LIFE = 10_000;
	static final int POINTS_ALL_GHOSTS = 12_000;
	static final int POINTS_GHOSTS[] = { 200, 400, 800, 1600 };
	static final int BONUS_ACTIVATION[] = { 70, 170 };

	/**
	 * Returns the level-specific data.
	 * 
	 * <img src="http://www.gamasutra.com/db_area/images/feature/3938/tablea1.png">
	 * 
	 * @param level level number (1..)
	 * @return data for level with given number
	 */
	static List<Object> levelData(int level) {
		if (level < 1) {
			throw new IllegalArgumentException("Illegal game level number: " + level);
		}
		switch (level) {
		/*@formatter:off*/
		case  1: return List.of("CHERRIES",   100,  80,  75, 40,  20,  80, 10,  85,  90, 50, 6, 5);
		case  2: return List.of("STRAWBERRY", 300,  90,  85, 45,  30,  90, 15,  95,  95, 55, 5, 5);
		case  3: return List.of("PEACH",      500,  90,  85, 45,  40,  90, 20,  95,  95, 55, 4, 5);
		case  4: return List.of("PEACH",      500,  90,  85, 50,  40, 100, 20,  95,  95, 55, 3, 5);
		case  5: return List.of("APPLE",      700, 100,  95, 50,  40, 100, 20, 105, 100, 60, 2, 5);
		case  6: return List.of("APPLE",      700, 100,  95, 50,  50, 100, 25, 105, 100, 60, 5, 5);
		case  7: return List.of("GRAPES",    1000, 100,  95, 50,  50, 100, 25, 105, 100, 60, 2, 5);
		case  8: return List.of("GRAPES",    1000, 100,  95, 50,  50, 100, 25, 105, 100, 60, 2, 5);
		case  9: return List.of("GALAXIAN",  2000, 100,  95, 50,  60, 100, 30, 105, 100, 60, 1, 3);
		case 10: return List.of("GALAXIAN",  2000, 100,  95, 50,  60, 100, 30, 105, 100, 60, 5, 5);
		case 11: return List.of("BELL",      3000, 100,  95, 50,  60, 100, 30, 105, 100, 60, 2, 5);
		case 12: return List.of("BELL",      3000, 100,  95, 50,  80, 100, 40, 105, 100, 60, 1, 3);
		case 13: return List.of("KEY",       5000, 100,  95, 50,  80, 100, 40, 105, 100, 60, 1, 3);
		case 14: return List.of("KEY",       5000, 100,  95, 50,  80, 100, 40, 105, 100, 60, 3, 5);
		case 15: return List.of("KEY",       5000, 100,  95, 50, 100, 100, 50, 105, 100, 60, 1, 3);
		case 16: return List.of("KEY",       5000, 100,  95, 50, 100, 100, 50, 105,   0,  0, 1, 3);
		case 17: return List.of("KEY",       5000, 100,  95, 50, 100, 100, 50, 105, 100, 60, 0, 0);
		case 18: return List.of("KEY",       5000, 100,  95, 50, 100, 100, 50, 105,   0,  0, 1, 0);
		case 19: return List.of("KEY",       5000, 100,  95, 50, 120, 100, 60, 105,   0,  0, 0, 0);
		case 20: return List.of("KEY",       5000, 100,  95, 50, 120, 100, 60, 105,   0,  0, 0, 0);
		default: return List.of("KEY",       5000,  90,  95, 50, 120, 100, 60, 105,   0,  0, 0, 0);
		//@formatter:on
		}
	}

	private static float percent(Object value) {
		return (int) value / 100f;
	}

	private static int integer(Object value) {
		return (int) value;
	}

	public static boolean started() {
		return game != null;
	}

	public static void start(int startLevel, int totalFoodCount) {
		game = new PacManGame(startLevel, totalFoodCount, PACMAN_LIVES, 0);
		game.hiscore = new Hiscore(new File(new File(System.getProperty("user.home")), "pacman.hiscore.xml"));
		game.levelCounter.add(game.bonusSymbol);
		loginfo("Game started at level %d", startLevel);
	}

	public static void nextLevel() {
		if (!started()) {
			throw new IllegalStateException("Cannot enter next level, game not started");
		}
		PacManGame next = new PacManGame(game.level + 1, game.foodCount, game.lives, game.score);
		next.hiscore = game.hiscore;
		next.levelCounter = game.levelCounter;
		next.levelCounter.add(next.bonusSymbol);
		game = next;
		loginfo("Game entered level %d" + "", next.level);
	}

	//@formatter:off
	
	public final String bonusSymbol;
	public final int    bonusValue;
	public final float  pacManSpeed;
	public final float  ghostSpeed;
	public final float  ghostTunnelSpeed;
	public final int    elroy1DotsLeft;
	public final float  elroy1Speed;
	public final int    elroy2DotsLeft;
	public final float  elroy2Speed;
	public final float  pacManPowerSpeed;
	public final float  ghostFrightenedSpeed;
	public final int    pacManPowerSeconds;
	public final int    numFlashes;

	public final int    level;
	public final int    foodCount;

	public int          eatenFoodCount;
	public int          ghostsKilledByEnergizer;
	public int          ghostsKilledInLevel;
	public int          lives;
	public int          score;
	public Hiscore      hiscore;
	public List<String> levelCounter;

	//@formatter:on

	private PacManGame(int level, int foodCount, int lives, int score) {
		this.level = level;
		this.foodCount = foodCount;
		this.lives = lives;
		this.score = score;
		this.levelCounter = new ArrayList<>();
		List<?> data = levelData(level);
		bonusSymbol = (String) data.get(0);
		bonusValue = integer(data.get(1));
		pacManSpeed = percent(data.get(2));
		ghostSpeed = percent(data.get(3));
		ghostTunnelSpeed = percent(data.get(4));
		elroy1DotsLeft = integer(data.get(5));
		elroy1Speed = percent(data.get(6));
		elroy2DotsLeft = integer(data.get(7));
		elroy2Speed = percent(data.get(8));
		pacManPowerSpeed = percent(data.get(9));
		ghostFrightenedSpeed = percent(data.get(10));
		pacManPowerSeconds = integer(data.get(11));
		numFlashes = integer(data.get(12));
	}

	public int remainingFoodCount() {
		return foodCount - eatenFoodCount;
	}

	/**
	 * Gains the given points and handles high score and extra life.
	 * 
	 * @param points gained points
	 * @return {@code true} if extra life has been gained
	 */
	private boolean gain(int points) {
		boolean extraLife = score < POINTS_EXTRA_LIFE && score + points >= POINTS_EXTRA_LIFE;
		if (extraLife) {
			lives++;
		}
		score += points;
		hiscore.check(level, score);
		return extraLife;
	}

	/**
	 * Gains bonus points.
	 */
	public boolean gainBonus() {
		return gain(bonusValue);
	}

	/**
	 * Gains points for eating an energizer.
	 */
	public boolean gainEnergizerPoints() {
		eatenFoodCount += 1;
		ghostsKilledByEnergizer = 0;
		return gain(POINTS_ENERGIZER);
	}

	/**
	 * Gains points for eating a normal pellet.
	 */
	public boolean gainPelletPoints() {
		eatenFoodCount += 1;
		return gain(POINTS_PELLET);
	}

	/**
	 * Gains points for killing a ghost. Value doubles for each ghost killed in series using the same energizer. If all
	 * ghosts have been killed in a level, additional 12000 points are gained.
	 */
	public boolean gainGhostPoints() {
		ghostsKilledByEnergizer += 1;
		ghostsKilledInLevel += 1;
		int ghostBounty = ghostBounty();
		loginfo("Got %d points for killing %s ghost", ghostBounty,
				new String[] { "", "first", "2nd", "3rd", "4th" }[ghostsKilledByEnergizer]);
		int points = ghostBounty;
		if (ghostsKilledInLevel == 16) {
			points += POINTS_ALL_GHOSTS;
		}
		return gain(points);
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
	public boolean isBonusGettingActivated() {
		return eatenFoodCount == BONUS_ACTIVATION[0] || eatenFoodCount == BONUS_ACTIVATION[1];
	}
}