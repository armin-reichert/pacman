package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

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
	static final Object[][] LEVEL_DATA = {
		/*@formatter:off*/
		{"CHERRIES",   100,  80,  71,  75,  40,  20,  80,  10,  85,  90, 79, 50, 6, 5},
		{"STRAWBERRY", 300,  90,  79,  85,  45,  30,  90,  15,  95,  95, 83, 55, 5, 5},
		{"PEACH",      500,  90,  79,  85,  45,  40,  90,  20,  95,  95, 83, 55, 4, 5},
		{"PEACH",      500,  90,  79,  85,  50,  40, 100,  20,  95,  95, 83, 55, 3, 5},
		{"APPLE",      700, 100,  87,  95,  50,  40, 100,  20, 105, 100, 87, 60, 2, 5},
		{"APPLE",      700, 100,  87,  95,  50,  50, 100,  25, 105, 100, 87, 60, 5, 5},
		{"GRAPES",    1000, 100,  87,  95,  50,  50, 100,  25, 105, 100, 87, 60, 2, 5},
		{"GRAPES",    1000, 100,  87,  95,  50,  50, 100,  25, 105, 100, 87, 60, 2, 5},
		{"GALAXIAN",  2000, 100,  87,  95,  50,  60, 100,  30, 105, 100, 87, 60, 1, 3},
		{"GALAXIAN",  2000, 100,  87,  95,  50,  60, 100,  30, 105, 100, 87, 60, 5, 5},
		{"BELL",      3000, 100,  87,  95,  50,  60, 100,  30, 105, 100, 87, 60, 2, 5},
		{"BELL",      3000, 100,  87,  95,  50,  80, 100,  40, 105, 100, 87, 60, 1, 3},
		{"KEY",       5000, 100,  87,  95,  50,  80, 100,  40, 105, 100, 87, 60, 1, 3},
		{"KEY",       5000, 100,  87,  95,  50,  80, 100,  40, 105, 100, 87, 60, 3, 5},
		{"KEY",       5000, 100,  87,  95,  50, 100, 100,  50, 105, 100, 87, 60, 1, 3},
		{"KEY",       5000, 100,  87,  95,  50, 100, 100,  50, 105,   0,  0,  0, 1, 3},
		{"KEY",       5000, 100,  87,  95,  50, 100, 100,  50, 105, 100, 87, 60, 0, 0},
		{"KEY",       5000, 100,  87,  95,  50, 100, 100,  50, 105,   0,   0, 0, 1, 0},
		{"KEY",       5000, 100,  87,  95,  50, 120, 100,  60, 105,   0,   0, 0, 0, 0},
		{"KEY",       5000, 100,  87,  95,  50, 120, 100,  60, 105,   0,   0, 0, 0, 0},
		{"KEY",       5000,  90,  79,  95,  50, 120, 100,  60, 105,   0,   0, 0, 0, 0}
		/*@formatter:on*/
	};

	public static final int POINTS_SIMPLE_PELLET = 10;
	public static final int POINTS_ENERGIZER = 50;
	public static final int POINTS_EXTRA_LIFE = 10_000;
	public static final int POINTS_KILLED_ALL_GHOSTS = 12_000;
	public static final int POINTS_BONUS[] = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };
	public static final int GHOST_BOUNTIES[] = { 200, 400, 800, 1600 };
	public static final int SNACK_FAT = 1;
	public static final int BIG_MEAL_FAT = 3;
	public static final int BONUS_ACTIVATION_1 = 70;
	public static final int BONUS_ACTIVATION_2 = 170;
	public static final int BONUS_SECONDS = 9;

	public GameLevel level;

	public void startLevel(int number, int foodCount) {
		level = new GameLevel(number, 3, 0, foodCount);
		loginfo("Level %d started", number);
	}

	public void nextLevel() {
		level = new GameLevel(level.number + 1, level);
		loginfo("Level %d started", level.number);
	}
}