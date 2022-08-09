/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacmanfsm.view.dashboard.states;

import de.amr.games.pacmanfsm.controller.creatures.ghost.GhostMentalState;
import de.amr.games.pacmanfsm.lib.Direction;
import de.amr.games.pacmanfsm.lib.Tile;

public enum ColumnInfo {

	//@formatter:off
	ON_STAGE("On stage", Boolean.class, true), 
	NAME("Actor", String.class, false), 
	TILE("Tile", Tile.class, false), 
	TARGET("Target", Tile.class, false),
	MOVE_DIR("Moves", Direction.class, false),
	WISH_DIR("Wants", Direction.class, false),
	SPEED("px/s", Float.class, false),
	STATE("State", Object.class, false),
	SANITY("Sanity", GhostMentalState.class, false),
	REMAINING("Remaining", Integer.class, false), 
	DURATION("Duration", Integer.class, false);
	//@formatter:on

	private ColumnInfo(String name, Class<?> columnClass, boolean editable) {
		this.columnName = name != null ? name : name();
		this.columnClass = columnClass;
		this.editable = editable;
	}

	public final String columnName;
	public final Class<?> columnClass;
	public final boolean editable;

	public static ColumnInfo at(int col) {
		return ColumnInfo.values()[col];
	}
}