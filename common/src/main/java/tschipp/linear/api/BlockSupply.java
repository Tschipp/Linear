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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * An extra source of blocks for a line, like a bag. Linear uses the player's inventory first, then
 * asks each supply in turn. The last block in hand is used up last. Register with
 * {@link LinearApi#registerSupply}.
 */
public interface BlockSupply
{
	/**
	 * How many of {@code block} this supply can provide right now, at most {@code wanted}. Called on
	 * both sides (the client uses it for the preview). Never return more than {@link #take} would give.
	 *
	 * @param block the block as the player holds it, count 1; compare with
	 *              {@code ItemStack.isSameItemSameComponents}
	 */
	int available(Player player, ItemStack block, int wanted);

	/**
	 * Removes up to {@code amount} of {@code block} from the supply. Server side.
	 *
	 * @return how many were removed
	 */
	int take(ServerPlayer player, ItemStack block, int amount);
}
