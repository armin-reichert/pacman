package de.amr.games.pacman.model.game;

import static de.amr.easy.game.Application.loginfo;

import java.util.List;

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
	static List<Object> levelData(int number) {
		if (number > 21) {
			number = 21;
		}
		switch (number) {
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
		case 21: return List.of("KEY",       5000,  90,  79,  95, 50, 120, 100, 60, 105,   0,   0, 0, 0, 0);
		/*@formatter:on*/
		default:
			throw new IllegalArgumentException("Illegal game level number: " + number);
		}
	}

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