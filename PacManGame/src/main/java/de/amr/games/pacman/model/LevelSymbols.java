package de.amr.games.pacman.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Level symbols displayed at the right lower corner.
 * 
 * @author Armin Reichert
 */
public class LevelSymbols implements Iterable<BonusSymbol> {

	private final Deque<BonusSymbol> list = new ArrayDeque<>(7);

	public int count() {
		return list.size();
	}

	@Override
	public Iterator<BonusSymbol> iterator() {
		return list.iterator();
	}

	public void clear() {
		list.clear();
	}

	public void add(BonusSymbol symbol) {
		if (list.size() == 7) {
			list.removeLast();
		}
		list.addFirst(symbol);
	}
}