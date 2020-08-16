package de.amr.games.pacman.controller.creatures.ghost;

import static de.amr.games.pacman.controller.creatures.ghost.GhostMentalState.ELROY1;
import static de.amr.games.pacman.controller.creatures.ghost.GhostMentalState.ELROY2;
import static de.amr.games.pacman.controller.creatures.ghost.GhostMentalState.HEALTHY;
import static de.amr.games.pacman.controller.creatures.ghost.GhostMentalState.TRANQUILIZED;
import static de.amr.games.pacman.controller.creatures.ghost.GhostState.SCATTERING;
import static de.amr.games.pacman.controller.steering.api.AnimalMaster.you;

import java.util.Objects;

import de.amr.statemachine.api.TransitionMatchStrategy;
import de.amr.statemachine.core.StateMachine;

/**
 * Blinky becomes mad (transforms into "Cruise Elroy") whenever the number of remaining food reaches
 * certain values that vary between game levels.
 * <p>
 * 
 * <cite> All ghosts move at the same rate of speed when a level begins, but Blinky will increase
 * his rate of speed twice each round based on the number of dots remaining in the maze. While in
 * this accelerated state, Blinky is commonly called "Cruise Elroy", yet no one seems to know where
 * this custom was originated or what it means.
 * 
 * On the first level, for example, Blinky becomes Elroy when there are 20 dots remaining in the
 * maze, accelerating to be at least as fast as Pac-Man. More importantly, his scatter mode behavior
 * is also modified to target Pac-Man's tile in lieu of his typical fixed target tile for any
 * remaining scatter periods in the level.
 * 
 * This causes Elroy to chase Pac-Man while the other three ghosts continue to scatter as normal. As
 * if that weren't bad enough, when only 10 dots remain, Elroy speeds up again to the point where he
 * is now perceptibly faster than Pac-Man.
 * 
 * If a life is lost any time after Blinky has become Elroy, he will revert back to his normal
 * behavior and speed when play resumes, heading for his home corner during the initial scatter
 * period. But once the last ghost (Clyde) has left the ghost house in the middle of the board, he
 * will turn back into Elroy again.
 * 
 * Keep in mind: he is still in scatter mode the entire time. All that has changed is the target
 * tile-he will still reverse direction when entering and exiting scatter mode as before. As the
 * levels progress, Blinky will turn into Elroy with more dots remaining in the maze than in
 * previous rounds. Refer to Table A.1 in the appendices for dot counts and speeds for both Elroy
 * changes, per level. </cite>
 * 
 * @author Armin Reichert
 * 
 * @see https://www.gamasutra.com/view/feature/132330/the_pacman_dossier.php?page=7
 */
public class GhostMadnessController extends StateMachine<GhostMentalState, Byte> {

	// events:
	private static final byte PACMAN_DIES = 0;
	private static final byte CLYDE_EXITS_HOUSE = 1;

	private final Ghost ghost;

	public GhostMadnessController(Ghost ghost) {
		super(GhostMentalState.class, TransitionMatchStrategy.BY_VALUE);
		this.ghost = Objects.requireNonNull(ghost);
		setMissingTransitionBehavior(MissingTransitionBehavior.LOG);
		//@formatter:off
		beginStateMachine()
			.initialState(HEALTHY)
			.description(() -> String.format("Ghost %s Madness", ghost.name))
			.states()
			
				.state(HEALTHY).onEntry(this::targetCorner)
				
				.state(ELROY1).onEntry(this::targetPacMan)
					
				.state(ELROY2).onEntry(this::targetPacMan)
					
				.state(TRANQUILIZED).onEntry(this::targetCorner)
			
			.transitions()
			
				.when(HEALTHY).then(ELROY2)
					.condition(this::reachedElroy2Score)
					.annotation(() -> String.format("Pellets left <= %d", ghost.game.level.elroy2DotsLeft))
			
				.when(HEALTHY).then(ELROY1)
					.condition(this::reachedElroy1Score)
					.annotation(() -> String.format("Pellets left <= %d", ghost.game.level.elroy1DotsLeft))

				.when(TRANQUILIZED).then(ELROY2)
					.on(CLYDE_EXITS_HOUSE)
					.condition(this::reachedElroy2Score)
					.annotation("Become Elroy again when Clyde exits house")
					
				.when(TRANQUILIZED).then(ELROY1)
					.on(CLYDE_EXITS_HOUSE)
					.condition(this::reachedElroy1Score)
					.annotation("Become Elroy again when Clyde exits house")
					
				.when(ELROY1).then(ELROY2)
					.condition(this::reachedElroy2Score)
					.annotation(() -> String.format("Remaining pellets <= %d", ghost.game.level.elroy2DotsLeft))

				.when(ELROY1).then(TRANQUILIZED).on(PACMAN_DIES)
					.annotation("Suspend Elroy when Pac-Man dies")
				
				.when(ELROY2).then(TRANQUILIZED).on(PACMAN_DIES)
					.annotation("Suspend Elroy when Pac-Man dies")
					
		.endStateMachine();
		//@formatter:on
		init();
	}

	private boolean reachedElroy1Score() {
		return ghost.game.level.remainingFoodCount() <= ghost.game.level.elroy1DotsLeft;
	}

	private boolean reachedElroy2Score() {
		return ghost.game.level.remainingFoodCount() <= ghost.game.level.elroy2DotsLeft;
	}

	private void targetCorner() {
		you(ghost).when(SCATTERING).headFor().tile(ghost.world.width() - 3, 0).ok();
	}

	private void targetPacMan() {
		you(ghost).when(SCATTERING).headFor().tile(ghost.pacMan.body::tile).ok();
	}

	public void pacManDies() {
		process(PACMAN_DIES);
	}

	public void clydeExitsHouse() {
		process(CLYDE_EXITS_HOUSE);
	}
}