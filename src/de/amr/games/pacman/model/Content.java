package de.amr.games.pacman.model;

public interface Content {

	// Structure
	public static final char EMPTY = ' ';
	public static final char WALL = '#';
	public static final char DOOR = 'D';
	public static final char TUNNEL = 'T';

	// Position markers
	public static final char POS_BONUS = '$';
	public static final char POS_PACMAN = 'O';
	public static final char POS_BLINKY = 'B';
	public static final char POS_INKY = 'I';
	public static final char POS_PINKY = 'P';
	public static final char POS_CLYDE = 'C';

	// Food
	public static final char PELLET = '.';
	public static final char ENERGIZER = '*';
	public static final char EATEN = ':';
}