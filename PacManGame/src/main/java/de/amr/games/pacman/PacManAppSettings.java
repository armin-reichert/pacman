package de.amr.games.pacman;

import com.beust.jcommander.Parameter;

import de.amr.easy.game.config.AppSettings;

/**
 * Settings for Pac-Man application.
 * 
 * @author Armin Reichert
 */
public class PacManAppSettings extends AppSettings {

	@Parameter(names = { "-demoMode" }, description = "Pac-Man moves automatically, stays alive when killed")
	public boolean demoMode = false;

	@Parameter(names = { "-ghostsFleeRandomly" }, description = "default frightened ghost behavior", arity = 1)
	public boolean ghostsFleeRandomly = true;

	@Parameter(names = { "-ghostsDangerous" }, description = "if set, ghosts can kill Pac-Man", arity = 1)
	public boolean ghostsDangerous = true;

	@Parameter(names = {
			"-overflowBug" }, description = "simulate the overflow bug from the original Arcade game", arity = 1)
	public boolean overflowBug = true;

	@Parameter(names = { "-pacManImmortable" }, description = "if set, Pac-Man keeps lives when killed")
	public boolean pacManImmortable = false;

	@Parameter(names = { "-skipIntro" }, description = "start app without intro screen")
	public boolean skipIntro = false;

	@Parameter(names = { "-theme" }, description = "the theme name e.g. 'Arcade'")
	public String theme = "Arcade";
}