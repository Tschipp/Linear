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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tschipp.linear.Constants;
import tschipp.linear.api.BlockSupply;
import tschipp.linear.api.HeldItemHandler;
import tschipp.linear.api.LinearApi;
import tschipp.linear.data.LinearSettings;

/** Where a line's blocks come from: a block in hand (plus inventory and supplies), or an item that picks blocks. */
public sealed interface BlockSource
{
	/** The block the preview shows. */
	ItemStack preview(Player player);

	/** How many blocks there are for the line, at most {@code wanted}. Creative players have no limit. */
	int available(Player player, int wanted);

	/** Takes the next block, or returns empty if there are none left. */
	ItemStack takeOne(ServerPlayer player);

	/** Puts back a block that was taken but not placed. */
	void giveBack(ServerPlayer player, ItemStack block);

	/** What the player can draw a line with in {@code hand}, or null if nothing. */
	@Nullable
	static BlockSource of(Player player, InteractionHand hand)
	{
		ItemStack held = player.getItemInHand(hand);
		if (held.isEmpty())
			return null;

		if (held.getItem() instanceof BlockItem)
			return allowed(held, LinearState.settings(player)) ? new HeldBlock(hand, held.copyWithCount(1)) : null;

		HeldItemHandler handler = LinearApi.handlerFor(held);
		return handler == null ? null : new HeldItem(hand, handler);
	}

	/** The hand to draw a line with (main hand first), or null. */
	@Nullable
	static InteractionHand drawingHand(Player player)
	{
		for (InteractionHand hand : InteractionHand.values())
			if (of(player, hand) != null)
				return hand;
		return null;
	}

	/** Whether a block may be used in a line, checking the blacklist or whitelist. */
	static boolean allowed(ItemStack block, LinearSettings settings)
	{
		if (!(block.getItem() instanceof BlockItem))
			return false;
		return settings.useWhitelist() ? block.is(Constants.WHITELIST) : !block.is(Constants.BLACKLIST);
	}

	/**
	 * A block in hand. Uses the held stack first, then matching stacks in the inventory, then each
	 * {@link BlockSupply}. The last block in hand is only used once everything else is empty.
	 */
	record HeldBlock(InteractionHand hand, ItemStack template) implements BlockSource
	{
		@Override
		public ItemStack preview(Player player)
		{
			return template;
		}

		@Override
		public int available(Player player, int wanted)
		{
			if (player.isCreative())
				return wanted;

			int found = 0;
			Inventory inventory = player.getInventory();
			for (int slot = 0; slot < inventory.getContainerSize() && found < wanted; slot++)
				if (matches(inventory.getItem(slot)))
					found += inventory.getItem(slot).getCount();

			for (BlockSupply supply : LinearApi.supplies())
			{
				if (found >= wanted)
					break;
				found += Math.max(0, supply.available(player, template.copy(), wanted - found));
			}

			return Math.min(found, wanted);
		}

		@Override
		public ItemStack takeOne(ServerPlayer player)
		{
			if (player.isCreative())
				return template.copy();

			ItemStack held = player.getItemInHand(hand);
			if (matches(held) && held.getCount() > 1)
			{
				held.shrink(1);
				return template.copy();
			}

			// Skip the held stack, its last block is kept for last.
			Inventory inventory = player.getInventory();
			for (int slot = 0; slot < inventory.getContainerSize(); slot++)
			{
				ItemStack stack = inventory.getItem(slot);
				if (stack != held && matches(stack))
				{
					stack.shrink(1);
					return template.copy();
				}
			}

			for (BlockSupply supply : LinearApi.supplies())
				if (supply.take(player, template.copy(), 1) > 0)
					return template.copy();

			if (matches(held))
			{
				held.shrink(1);
				return template.copy();
			}

			return ItemStack.EMPTY;
		}

		@Override
		public void giveBack(ServerPlayer player, ItemStack block)
		{
			if (player.isCreative())
				return;

			ItemStack held = player.getItemInHand(hand);
			if (held.isEmpty())
				player.setItemInHand(hand, block);
			else if (matches(held) && held.getCount() + block.getCount() <= held.getMaxStackSize())
				held.grow(block.getCount());
			else
				player.getInventory().placeItemBackInInventory(block);
		}

		private boolean matches(ItemStack stack)
		{
			return !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, template);
		}
	}

	/** A non-block item that places blocks, see {@link HeldItemHandler}. */
	record HeldItem(InteractionHand hand, HeldItemHandler handler) implements BlockSource
	{
		@Override
		public ItemStack preview(Player player)
		{
			return handler.preview(player, player.getItemInHand(hand));
		}

		@Override
		public int available(Player player, int wanted)
		{
			return player.isCreative() ? wanted : Math.min(wanted, handler.available(player, player.getItemInHand(hand), wanted));
		}

		@Override
		public ItemStack takeOne(ServerPlayer player)
		{
			return handler.takeNext(player, player.getItemInHand(hand));
		}

		@Override
		public void giveBack(ServerPlayer player, ItemStack block)
		{
			handler.giveBack(player, player.getItemInHand(hand), block);
		}
	}
}
