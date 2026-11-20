/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2026 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.linear.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildGeometryTest
{
	private static final BlockPos ORIGIN = new BlockPos(0, 0, 0);

	@Test
	@DisplayName("a free line reaches from start to end without gaps, in every direction and at every length")
	void freeLinesAreUnbroken()
	{
		for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++)
				for (int dz = -1; dz <= 1; dz++)
					for (int length : new int[]{1, 2, 5, 13})
						for (int skew : new int[]{0, 1, 3})
						{
							BlockPos end = new BlockPos(dx * length, dy * Math.max(0, length - skew), dz * length + skew);
							List<BlockPos> line = BuildGeometry.positions(BuildMode.LINE_3_AXES, ORIGIN, end);
							assertUnbroken(line, ORIGIN, end);
						}
	}

	@Test
	@DisplayName("lines crossing zero come out right")
	void crossingZero()
	{
		BlockPos start = new BlockPos(-2, 64, -3);
		BlockPos end = new BlockPos(3, 64, 4);

		// Z changes most (7 against 5), so a one-axis line runs along Z only, through z = 0.
		List<BlockPos> line = BuildGeometry.positions(BuildMode.LINE_1_AXIS, start, end);
		assertEquals(8, line.size());
		for (BlockPos pos : line)
			assertEquals(-2, pos.getX(), "a one-axis line keeps the start's X: " + pos);
		assertTrue(line.contains(new BlockPos(-2, 64, 0)));
		assertTrue(line.contains(new BlockPos(-2, 64, 4)));

		assertUnbroken(BuildGeometry.positions(BuildMode.LINE_3_AXES, start, end), start, end);
	}

	@Test
	@DisplayName("a two-axis line holds the axis that changes least, a one-axis line keeps only the one that changes most")
	void fewerAxes()
	{
		List<BlockPos> twoAxes = BuildGeometry.positions(BuildMode.LINE_2_AXES, ORIGIN, new BlockPos(5, 1, 3));
		assertEquals(6, twoAxes.size());
		for (BlockPos pos : twoAxes)
			assertEquals(0, pos.getY(), "Y changed least, so the line stays at the start's height: " + pos);
		assertTrue(twoAxes.contains(new BlockPos(5, 0, 3)));

		// A perfect diagonal: X wins the tie, so the line runs along the ground, not up.
		List<BlockPos> oneAxis = BuildGeometry.positions(BuildMode.LINE_1_AXIS, ORIGIN, new BlockPos(3, 3, 3));
		assertEquals(List.of(ORIGIN, new BlockPos(1, 0, 0), new BlockPos(2, 0, 0), new BlockPos(3, 0, 0)), oneAxis);
	}

	@Test
	@DisplayName("walls rise from the start's height to the end's, straight or diagonal, upward or downward")
	void walls()
	{
		// X changes more than Z, so the axis-aligned wall runs along X at the start's Z.
		Set<BlockPos> straight = new HashSet<>(BuildGeometry.positions(BuildMode.WALL_2_AXES, ORIGIN, new BlockPos(3, 2, 1)));
		assertEquals(4 * 3, straight.size());
		for (BlockPos pos : straight)
			assertEquals(0, pos.getZ(), pos.toString());
		assertTrue(straight.contains(new BlockPos(3, 2, 0)));

		Set<BlockPos> diagonal = new HashSet<>(BuildGeometry.positions(BuildMode.WALL_3_AXES, ORIGIN, new BlockPos(3, 2, 3)));
		assertEquals(4 * 3, diagonal.size());
		for (int i = 0; i <= 3; i++)
			for (int y = 0; y <= 2; y++)
				assertTrue(diagonal.contains(new BlockPos(i, y, i)), "missing " + i + "," + y + "," + i);

		Set<BlockPos> down = new HashSet<>(BuildGeometry.positions(BuildMode.WALL_2_AXES, new BlockPos(0, 5, 0), new BlockPos(2, 3, 0)));
		assertEquals(3 * 3, down.size());
		assertTrue(down.contains(new BlockPos(2, 3, 0)) && down.contains(new BlockPos(0, 5, 0)));
	}

	@Test
	@DisplayName("a wall between two blocks at the same height is a floor, whatever the signs")
	void floors()
	{
		for (BuildMode mode : new BuildMode[]{BuildMode.WALL_2_AXES, BuildMode.WALL_3_AXES})
		{
			Set<BlockPos> floor = new HashSet<>(BuildGeometry.positions(mode, new BlockPos(1, 5, 2), new BlockPos(-1, 5, -1)));
			assertEquals(3 * 4, floor.size(), mode.getSerializedName());
			for (int x = -1; x <= 1; x++)
				for (int z = -1; z <= 2; z++)
					assertTrue(floor.contains(new BlockPos(x, 5, z)), mode.getSerializedName() + " is missing " + x + ",5," + z);
		}
	}

	@Test
	@DisplayName("a circle is a gap-free ring around the start through the end, flat on the ground for a flat drag")
	void circles()
	{
		for (BlockPos end : new BlockPos[]{new BlockPos(5, 0, 0), new BlockPos(3, 0, -4), new BlockPos(-2, 0, 7), new BlockPos(1, 0, 1)})
		{
			List<BlockPos> ring = BuildGeometry.positions(BuildMode.CIRCLE_2_AXES, ORIGIN, end);
			assertTrue(ring.contains(end), "the ring should pass through " + end);
			assertFalse(ring.contains(ORIGIN), "the centre stays empty");
			for (BlockPos pos : ring)
				assertEquals(0, pos.getY(), "a flat drag makes a flat circle: " + pos);
			assertSealed(ring, ORIGIN, true);
		}

		List<BlockPos> five = BuildGeometry.positions(BuildMode.CIRCLE_2_AXES, ORIGIN, new BlockPos(5, 0, 0));
		for (BlockPos tip : new BlockPos[]{new BlockPos(-5, 0, 0), new BlockPos(0, 0, 5), new BlockPos(0, 0, -5)})
			assertTrue(five.contains(tip), "a circle of radius 5 reaches " + tip);
	}

	@Test
	@DisplayName("a circle stands up when the drag goes up, held at the start along the axis that changes least")
	void standingCircles()
	{
		List<BlockPos> up = BuildGeometry.positions(BuildMode.CIRCLE_2_AXES, ORIGIN, new BlockPos(0, 4, 0));
		for (BlockPos pos : up)
			assertEquals(0, pos.getZ(), "Z ties with X and comes first, so the circle stands in the X-Y plane: " + pos);
		assertTrue(up.contains(new BlockPos(0, -4, 0)) && up.contains(new BlockPos(4, 0, 0)));

		List<BlockPos> tilted = BuildGeometry.positions(BuildMode.CIRCLE_2_AXES, ORIGIN, new BlockPos(4, 3, 1));
		for (BlockPos pos : tilted)
			assertEquals(0, pos.getZ(), "Z changed least: " + pos);
		assertTrue(tilted.contains(new BlockPos(4, 3, 0)));
		assertSealed(tilted, ORIGIN, true);
	}

	@Test
	@DisplayName("a sphere is a hollow shell around the start through the end, with no holes")
	void spheres()
	{
		for (BlockPos end : new BlockPos[]{new BlockPos(1, 0, 0), new BlockPos(4, 0, 0), new BlockPos(2, -3, 5), new BlockPos(6, 6, 6)})
		{
			List<BlockPos> shell = BuildGeometry.positions(BuildMode.SPHERE_3_AXES, ORIGIN, end);
			assertTrue(shell.contains(end), "the shell should pass through " + end);
			assertFalse(shell.contains(ORIGIN), "the centre stays empty");
			assertSealed(shell, ORIGIN, false);

			double radius = Math.sqrt(ORIGIN.distSqr(end));
			for (BlockPos pos : shell)
				assertTrue(Math.sqrt(ORIGIN.distSqr(pos)) <= radius + 0.5, pos + " is outside the sphere through " + end);
		}

		// Radius 1: the 3x3x3 cube without its centre and corners.
		assertEquals(18, BuildGeometry.positions(BuildMode.SPHERE_3_AXES, ORIGIN, new BlockPos(0, 1, 0)).size());
	}

	@Test
	@DisplayName("a drag that does not move covers just the one block, in every mode")
	void singleBlock()
	{
		for (BuildMode mode : BuildMode.values())
			assertEquals(List.of(ORIGIN), BuildGeometry.positions(mode, ORIGIN, ORIGIN), mode.getSerializedName());
	}

	@Test
	@DisplayName("no mode lists a position twice")
	void noDuplicates()
	{
		BlockPos end = new BlockPos(-7, 4, 9);
		for (BuildMode mode : BuildMode.values())
		{
			List<BlockPos> positions = BuildGeometry.positions(mode, ORIGIN, end);
			assertEquals(new HashSet<>(positions).size(), positions.size(), mode.getSerializedName());
		}
	}

	/**
	 * Walking from the centre through faces (only within the shape's plane for a circle) never gets past
	 * the shape, so it has no holes.
	 */
	private static void assertSealed(List<BlockPos> shape, BlockPos centre, boolean flat)
	{
		Set<BlockPos> walls = new HashSet<>(shape);
		int bound = 0;
		for (BlockPos pos : shape)
			bound = Math.max(bound, Math.max(Math.abs(pos.getX() - centre.getX()),
					Math.max(Math.abs(pos.getY() - centre.getY()), Math.abs(pos.getZ() - centre.getZ()))));

		// The plane a circle lies in is the one its blocks all share a coordinate of.
		boolean sameX = shape.stream().allMatch(pos -> pos.getX() == centre.getX());
		boolean sameY = shape.stream().allMatch(pos -> pos.getY() == centre.getY());
		boolean sameZ = shape.stream().allMatch(pos -> pos.getZ() == centre.getZ());

		Set<BlockPos> seen = new HashSet<>(Set.of(centre));
		List<BlockPos> queue = new ArrayList<>(List.of(centre));
		while (!queue.isEmpty())
		{
			BlockPos pos = queue.remove(queue.size() - 1);
			for (Direction direction : Direction.values())
			{
				if (flat && ((sameX && direction.getStepX() != 0) || (sameY && direction.getStepY() != 0)
						|| (sameZ && direction.getStepZ() != 0)))
					continue;

				BlockPos next = pos.relative(direction);
				if (walls.contains(next) || !seen.add(next))
					continue;
				assertTrue(Math.abs(next.getX() - centre.getX()) <= bound && Math.abs(next.getY() - centre.getY()) <= bound
						&& Math.abs(next.getZ() - centre.getZ()) <= bound, "the shape has a hole near " + pos);
				queue.add(next);
			}
		}
	}

	/** Starts at start, ends at end, one position per step of the axis that changes most, each touching the last. */
	private static void assertUnbroken(List<BlockPos> line, BlockPos start, BlockPos end)
	{
		int steps = Math.max(Math.abs(end.getX() - start.getX()),
				Math.max(Math.abs(end.getY() - start.getY()), Math.abs(end.getZ() - start.getZ())));
		assertEquals(steps + 1, line.size(), "from " + start + " to " + end);
		assertEquals(start, line.get(0));
		assertEquals(end, line.get(line.size() - 1));

		for (int i = 1; i < line.size(); i++)
		{
			BlockPos a = line.get(i - 1);
			BlockPos b = line.get(i);
			assertTrue(Math.abs(a.getX() - b.getX()) <= 1 && Math.abs(a.getY() - b.getY()) <= 1
					&& Math.abs(a.getZ() - b.getZ()) <= 1, "gap between " + a + " and " + b + " on the way to " + end);
		}
	}
}
