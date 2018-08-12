package de.amr.games.pacman.model;

public class Counter {

	protected int count;

	public void set(int n) {
		count = n;
	}

	public void add(int n) {
		count += n;
	}

	public void sub(int n) {
		count -= n;
	}

	public int get() {
		return count;
	}
}
