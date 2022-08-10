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
package de.amr.games.pacmanfsm.controller.creatures.ghost;

import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostMentalState.ELROY1;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostMentalState.ELROY2;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostMentalState.HEALTHY;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostMentalState.TRANQUILIZED;
import static de.amr.games.pacmanfsm.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacmanfsm.controller.game.GameController.theGame;
import static de.amr.games.pacmanfsm.controller.steering.api.SteeringBuilder.you;

import java.util.Objects;

import de.amr.games.pacmanfsm.controller.creatures.pacman.PacMan;
import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.MissingTransitionBehavior;
import de.amr.statemachine.core.StateMachine;

/**
 * Blinky becomes mad (transforms into "Cruise Elroy") whenever the number of remaining food reaches certain values that
 * vary between game levels.
 * <p>
 * 
 * <cite> All ghosts move at the same rate of speed when a level begins, but Blinky will increase his rate of speed
 * twice each round based on the number of dots remaining in the maze. While in this accelerated state, Blinky is
 * commonly called "Cruise Elroy", yet no one seems to know where this custom was originated or what it means.
 * 
 * On the first level, for example, Blinky becomes Elroy when there are 20 dots remaining in the maze, accelerating to
 * be at least as fast as Pac-Man. More importantly, his scatter mode behavior is also modified to target Pac-Man's tile
 * in lieu of his typical fixed target tile for any remaining scatter periods in the level.
 * 
 * This causes Elroy to chase Pac-Man while the other three ghosts continue to scatter as normal. As if that weren't bad
 * enough, when only 10 dots remain, Elroy speeds up again to the point where he is now perceptibly faster than Pac-Man.
 * 
 * If a life is lost any time after Blinky has become Elroy, he will revert back to his normal behavior and speed when
 * play resumes, heading for his home corner during the initial scatter period. But once the last ghost (Clyde) has left
 * the ghost house in the middle of the board, he will turn back into Elroy again.
 * 
 * Keep in mind: he is still in scatter mode the entire time. All that has changed is the target tile-he will still
 * reverse direction when entering and exiting scatter mode as before. As the levels progress, Blinky will turn into
 * Elroy with more dots remaining in the maze than in previous rounds. Refer to Table A.1 in the appendices for dot
 * counts and speeds for both Elroy changes, per level. </cite>
 * 
 * @author Armin Reichert
 * 
 * @see https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=7
 */
public class GhostMadness extends StateMachine<GhostMentalState, String> {

	// events
	private static final String PACMAN_DIES = "Pac-Man dies";
	private static final String CLYDE_EXITS_HOUSE = "Clyde exits house";

	private final Ghost ghost;
	private final PacMan pacMan;

	public GhostMadness(Ghost ghost, PacMan pacMan) {
		super(GhostMentalState.class, TransitionMatchStrategy.BY_VALUE);
		this.ghost = Objects.requireNonNull(ghost);
		this.pacMan = Objects.requireNonNull(pacMan);
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		//@formatter:off
		beginStateMachine()
			.initialState(HEALTHY)
			.description(() -> String.format("%s Madness", ghost.name))
			.states()
			
				.state(HEALTHY).onEntry(this::headForCorner)
				
				.state(ELROY1).onEntry(this::headForPacMan)
					
				.state(ELROY2).onEntry(this::headForPacMan)
					
				.state(TRANQUILIZED).onEntry(this::headForCorner)
			
			.transitions()
			
				.when(HEALTHY).then(ELROY2)
					.condition(this::elroy2ScoreReached)
					.annotation(() -> String.format("Pellets left <= %d", theGame().elroy2DotsLeft))
			
				.when(HEALTHY).then(ELROY1)
					.condition(this::elroy1ScoreReached)
					.annotation(() -> String.format("Pellets left <= %d", theGame().elroy1DotsLeft))

				.when(TRANQUILIZED).then(ELROY2)
					.on(CLYDE_EXITS_HOUSE)
					.condition(this::elroy2ScoreReached)
					.annotation("Become Elroy again when Clyde exits house")
					
				.when(TRANQUILIZED).then(ELROY1)
					.on(CLYDE_EXITS_HOUSE)
					.condition(this::elroy1ScoreReached)
					.annotation("Become Elroy again when Clyde exits house")
					
				.when(ELROY1).then(ELROY2)
					.condition(this::elroy2ScoreReached)
					.annotation(() -> String.format("Remaining pellets <= %d", theGame().elroy2DotsLeft))

				.when(ELROY1).then(TRANQUILIZED).on(PACMAN_DIES)
					.annotation("Suspend Elroy when Pac-Man dies")
				
				.when(ELROY2).then(TRANQUILIZED).on(PACMAN_DIES)
					.annotation("Suspend Elroy when Pac-Man dies")
					
		.endStateMachine();
		//@formatter:on
		init();
	}

	public void pacManDies() {
		process(PACMAN_DIES);
	}

	public void clydeExitsHouse() {
		process(CLYDE_EXITS_HOUSE);
	}

	private boolean elroy1ScoreReached() {
		return theGame().remainingFoodCount() <= theGame().elroy1DotsLeft;
	}

	private boolean elroy2ScoreReached() {
		return theGame().remainingFoodCount() <= theGame().elroy2DotsLeft;
	}

	private void headForCorner() {
		you(ghost).when(SCATTERING).headFor().tile(ghost.world.width() - 3, 0).ok();
	}

	private void headForPacMan() {
		you(ghost).when(SCATTERING).headFor().tile(pacMan::tile).ok();
	}
}