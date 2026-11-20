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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Which positions a drag from {@code start} to {@code end} covers, for each {@link BuildMode}.
 * Doesn't check whether blocks can actually be placed there.
 * <ul>
 *     <li>{@code line_3_axes}: a straight line in any direction.</li>
 *     <li>{@code line_2_axes}: the same, but the axis that changes least stays at the start's value.</li>
 *     <li>{@code line_1_axis}: only along the axis that changes most.</li>
 *     <li>{@code wall_2_axes}: a wall along the horizontal axis that changes most, or a floor if start
 *     and end are at the same height.</li>
 *     <li>{@code wall_3_axes}: the same, but the wall can run diagonally.</li>
 *     <li>{@code circle_2_axes}: a ring around the start through the end, in the plane of the two axes
 *     that change most.</li>
 *     <li>{@code sphere_3_axes}: a hollow sphere around the start through the end.</li>
 * </ul>
 * Ties between axes prefer horizontal over Y, and X over Z.
 */
public final class BuildGeometry
{
	private BuildGeometry()
	{
	}

	/** Every position the drag covers, each once. */
	public static List<BlockPos> positions(BuildMode mode, BlockPos start, BlockPos end)
	{
		return switch (mode)
		{
			case LINE_3_AXES -> line(start, end);
			case LINE_2_AXES -> line(start, keep(start, end, leastChanging(start, end), false));
			case LINE_1_AXIS -> line(start, keep(start, end, mostChanging(start, end), true));
			case WALL_3_AXES -> wall(start, end, false);
			case WALL_2_AXES -> wall(start, end, true);
			case CIRCLE_2_AXES ->
			{
				Axis flat = leastChanging(start, end);
				yield shell(start, keep(start, end, flat, false), flat);
			}
			case SPHERE_3_AXES -> shell(start, end, null);
		};
	}

	/** Every position from {@code a} to {@code b}, both included, one step at a time along the main axis. */
	static List<BlockPos> line(BlockPos a, BlockPos b)
	{
		int dx = b.getX() - a.getX();
		int dy = b.getY() - a.getY();
		int dz = b.getZ() - a.getZ();
		int steps = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));

		List<BlockPos> positions = new ArrayList<>(steps + 1);
		for (int i = 0; i <= steps; i++)
		{
			positions.add(new BlockPos(
					a.getX() + scaled(dx, i, steps),
					a.getY() + scaled(dy, i, steps),
					a.getZ() + scaled(dz, i, steps)));
		}
		return positions;
	}

	/** {@code delta * i / steps}, rounded to the nearest whole block, halves away from zero. */
	private static int scaled(int delta, int i, int steps)
	{
		if (steps == 0)
			return 0;
		long twice = 2L * delta * i;
		long rounded = (Math.abs(twice) + steps) / (2L * steps);
		return (int) Math.copySign(rounded, delta);
	}

	private static List<BlockPos> wall(BlockPos start, BlockPos end, boolean axisAligned)
	{
		Set<BlockPos> positions = new LinkedHashSet<>();

		if (start.getY() == end.getY())
		{
			// A floor: the whole rectangle, in either mode.
			for (int x = Math.min(start.getX(), end.getX()); x <= Math.max(start.getX(), end.getX()); x++)
				for (int z = Math.min(start.getZ(), end.getZ()); z <= Math.max(start.getZ(), end.getZ()); z++)
					positions.add(new BlockPos(x, start.getY(), z));
			return new ArrayList<>(positions);
		}

		BlockPos baseEnd = new BlockPos(end.getX(), start.getY(), end.getZ());
		if (axisAligned)
			baseEnd = Math.abs(end.getX() - start.getX()) >= Math.abs(end.getZ() - start.getZ())
					? new BlockPos(end.getX(), start.getY(), start.getZ())
					: new BlockPos(start.getX(), start.getY(), end.getZ());

		int step = end.getY() > start.getY() ? 1 : -1;
		for (BlockPos base : line(start, baseEnd))
			for (int y = start.getY(); y != end.getY() + step; y += step)
				positions.add(new BlockPos(base.getX(), y, base.getZ()));

		return new ArrayList<>(positions);
	}

	/**
	 * The outer layer of a ball around {@code center} that reaches {@code edge}: every block inside it
	 * with a neighbour outside. Gives a ring (or sphere) without gaps, and always includes {@code edge}.
	 * With a {@code flat} axis it's a circle, one block thick along that axis.
	 */
	private static List<BlockPos> shell(BlockPos center, BlockPos edge, Axis flat)
	{
		// Half a block extra makes the shape rounder; without it the four tips stick out.
		double radius = Math.sqrt(center.distSqr(edge)) + 0.5;
		double limit = radius * radius;
		int size = (int) Math.ceil(radius);

		int sizeX = flat == Axis.X ? 0 : size;
		int sizeY = flat == Axis.Y ? 0 : size;
		int sizeZ = flat == Axis.Z ? 0 : size;

		List<BlockPos> positions = new ArrayList<>();
		for (int x = -sizeX; x <= sizeX; x++)
			for (int y = -sizeY; y <= sizeY; y++)
				for (int z = -sizeZ; z <= sizeZ; z++)
				{
					if (!inside(x, y, z, limit))
						continue;

					boolean outer = (sizeX > 0 && (!inside(x + 1, y, z, limit) || !inside(x - 1, y, z, limit)))
							|| (sizeY > 0 && (!inside(x, y + 1, z, limit) || !inside(x, y - 1, z, limit)))
							|| (sizeZ > 0 && (!inside(x, y, z + 1, limit) || !inside(x, y, z - 1, limit)));
					if (outer)
						positions.add(center.offset(x, y, z));
				}
		return positions;
	}

	private static boolean inside(int x, int y, int z, double limit)
	{
		return (double) x * x + (double) y * y + (double) z * z <= limit;
	}

	private enum Axis
	{
		X, Y, Z
	}

	private static int change(BlockPos a, BlockPos b, Axis axis)
	{
		return switch (axis)
		{
			case X -> Math.abs(b.getX() - a.getX());
			case Y -> Math.abs(b.getY() - a.getY());
			case Z -> Math.abs(b.getZ() - a.getZ());
		};
	}

	/** Ties go to X, then Z, then Y, so lines prefer the ground. */
	private static Axis mostChanging(BlockPos a, BlockPos b)
	{
		Axis best = Axis.X;
		for (Axis axis : new Axis[]{Axis.Z, Axis.Y})
			if (change(a, b, axis) > change(a, b, best))
				best = axis;
		return best;
	}

	/** Ties go to Y, then Z, then X, so two-axis lines stay flat. */
	private static Axis leastChanging(BlockPos a, BlockPos b)
	{
		Axis least = Axis.Y;
		for (Axis axis : new Axis[]{Axis.Z, Axis.X})
			if (change(a, b, axis) < change(a, b, least))
				least = axis;
		return least;
	}

	/** Takes {@code axis} from {@code end} (or every other axis if not {@code only}), the rest from {@code start}. */
	private static BlockPos keep(BlockPos start, BlockPos end, Axis axis, boolean only)
	{
		return new BlockPos(
				(axis == Axis.X) == only ? end.getX() : start.getX(),
				(axis == Axis.Y) == only ? end.getY() : start.getY(),
				(axis == Axis.Z) == only ? end.getZ() : start.getZ());
	}
}
