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

package tschipp.linear.api;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tschipp.linear.build.LinearState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Linear's API for other mods. Works the same on every loader; register during common setup, after
 * checking that Linear is loaded.
 */
public final class LinearApi
{
	private static final List<BlockSupply> SUPPLIES = new CopyOnWriteArrayList<>();
	private static final List<HeldItemHandler> HELD_ITEMS = new CopyOnWriteArrayList<>();

	private LinearApi()
	{
	}

	/** Adds a source of blocks besides the player's inventory. Supplies are asked in order. */
	public static void registerSupply(BlockSupply supply)
	{
		SUPPLIES.add(supply);
	}

	/** Lets a non-block item draw lines. The first matching handler is used. */
	public static void registerHeldItem(HeldItemHandler handler)
	{
		HELD_ITEMS.add(handler);
	}

	public static List<BlockSupply> supplies()
	{
		return SUPPLIES;
	}

	@Nullable
	public static HeldItemHandler handlerFor(ItemStack held)
	{
		for (HeldItemHandler handler : HELD_ITEMS)
			if (handler.handles(held))
				return handler;
		return null;
	}

	/** Whether the player has building enabled. Works on the server and for the local player. */
	public static boolean isBuildingEnabled(Player player)
	{
		return LinearState.isBuilding(player);
	}

	/** Whether the player is dragging a line. Client side, local player only. */
	public static boolean isDragging(Player player)
	{
		return LinearState.isDragging(player);
	}
}
