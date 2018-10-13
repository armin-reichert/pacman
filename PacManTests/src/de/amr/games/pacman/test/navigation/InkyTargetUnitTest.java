package de.amr.games.pacman.test.navigation;

import org.junit.Assert;
import org.junit.Test;

import de.amr.easy.game.math.Vector2f;
import de.amr.games.pacman.navigation.ActorNavigationSystem;

public class InkyTargetUnitTest {

	@Test
	public void testInkyTarget() {
		int w = 100;
		int h = 100;
		Vector2f b;
		Vector2f p;
		Vector2f t;

		// diagonal inside
		b = Vector2f.of(0, 0);
		p = Vector2f.of(10, 10);
		t = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
		Assert.assertEquals(t.x, 20, .001f);
		Assert.assertEquals(t.y, 20, .001f);

		// diagonal to lower-right corner
		b = Vector2f.of(0, 0);
		p = Vector2f.of(50, 50);
		t = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
		Assert.assertEquals(t.x, 100, .001f);
		Assert.assertEquals(t.y, 100, .001f);

		// vertical inside
		b = Vector2f.of(50, 50);
		p = Vector2f.of(50, 60);
		t = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
		Assert.assertEquals(t.x, 50, .001f);
		Assert.assertEquals(t.y, 70, .001f);

		// horizontal inside
		b = Vector2f.of(50, 50);
		p = Vector2f.of(60, 50);
		t = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
		Assert.assertEquals(t.x, 70, .001f);
		Assert.assertEquals(t.y, 50, .001f);

		// vertical outside
		b = Vector2f.of(50, 50);
		p = Vector2f.of(50, 90);
		t = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
		Assert.assertEquals(t.x, 50, .001f);
		Assert.assertEquals(t.y, 100, .001f);

		// horizontal outside
		b = Vector2f.of(50, 50);
		p = Vector2f.of(90, 50);
		t = ActorNavigationSystem.computeExactInkyTarget(b, p, w, h);
		Assert.assertEquals(t.x, 100, .001f);
		Assert.assertEquals(t.y, 50, .001f);

	}

}
