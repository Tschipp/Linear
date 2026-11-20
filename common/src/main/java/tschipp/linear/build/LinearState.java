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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.data.LinearSettings;
import tschipp.linear.platform.Services;

/**
 * A player's Linear state on either side. The server reads the attached data, the client uses what the
 * server last sent ({@link Client}).
 */
public final class LinearState
{
	private LinearState()
	{
	}

	/** The attached data. Server only. */
	public static LinearPlayerData data(Player player)
	{
		return Services.PLATFORM.getPlayerData(player);
	}

	public static boolean isBuilding(Player player)
	{
		return player.level().isClientSide() ? Client.building : data(player).building;
	}

	public static BuildMode mode(Player player)
	{
		return player.level().isClientSide() ? Client.mode : data(player).mode;
	}

	public static LinearSettings settings(Player player)
	{
		return player.level().isClientSide() ? Client.settings() : LinearSettings.of(data(player));
	}

	public static boolean isDragging(Player player)
	{
		return player.level().isClientSide() && Client.drag != null;
	}

	/** The local player's state as last sent by the server, plus the current drag. */
	public static final class Client
	{
		public static boolean building = true;
		public static BuildMode mode = BuildMode.LINE_3_AXES;
		@Nullable
		private static LinearSettings settings;
		@Nullable
		public static Drag drag;

		private Client()
		{
		}

		/** Config defaults until the server sends the real values. */
		public static LinearSettings settings()
		{
			if (settings == null)
				settings = LinearSettings.defaults();
			return settings;
		}

		public static void update(boolean building, BuildMode mode, LinearSettings settings)
		{
			Client.building = building;
			Client.mode = mode;
			Client.settings = settings;
		}

		/** Resets to defaults when leaving a world. */
		public static void reset()
		{
			building = true;
			mode = BuildMode.LINE_3_AXES;
			settings = null;
			drag = null;
		}
	}

	/** A line being dragged. The face and hit offset of the first click are used for every block. */
	public record Drag(BlockPos start, InteractionHand hand, Direction face, Vec3 hitOffset)
	{
	}
}
