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
 * Lets a non-block item that places blocks (like a bag) draw lines. It picks the block for each
 * position, so it can differ along the line. Register with {@link LinearApi#registerHeldItem}.
 */
public interface HeldItemHandler
{
	/** Whether this handler is for the item. */
	boolean handles(ItemStack held);

	/** The block the preview shows, or empty. Client side, called every frame. */
	ItemStack preview(Player player, ItemStack held);

	/**
	 * How many blocks the item can place right now, at most {@code wanted}. Both sides. A lower bound is
	 * fine, placing continues until {@link #takeNext} returns empty.
	 */
	int available(Player player, ItemStack held, int wanted);

	/**
	 * Takes the next block to place (count 1), or returns empty when there's nothing left. Server side.
	 * Creative players don't use anything up.
	 */
	ItemStack takeNext(ServerPlayer player, ItemStack held);

	/** Returns a block from {@link #takeNext} that couldn't be placed. */
	void giveBack(ServerPlayer player, ItemStack held, ItemStack block);
}
