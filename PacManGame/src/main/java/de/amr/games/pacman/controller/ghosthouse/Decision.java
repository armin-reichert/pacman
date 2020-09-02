package de.amr.games.pacman.controller.ghosthouse;

class Decision {

	public boolean confirmed;
	public String reason;

	static Decision confirmed(String msg, Object... args) {
		Decision d = new Decision();
		d.confirmed = true;
		d.reason = String.format(msg, args);
		return d;
	}

	static Decision rejected(String msg, Object... args) {
		Decision d = new Decision();
		d.confirmed = false;
		d.reason = String.format(msg, args);
		return d;
	}
}