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

package tschipp.linear.events;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import tschipp.linear.LinearCommon;
import tschipp.linear.LinearFabric;

public class CommonEvents
{
	public static void register()
	{
		// copyOnDeath doesn't cover dimension changes, so copy here for both.
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
				newPlayer.getAttachedOrCreate(LinearFabric.PLAYER_DATA).copyFrom(oldPlayer.getAttachedOrCreate(LinearFabric.PLAYER_DATA)));

		// A click that starts or ends a line does nothing else. Runs on both sides.
		UseBlockCallback.EVENT.register((player, level, hand, hit) ->
				LinearCommon.holdsBackUse(player) ? InteractionResult.FAIL : InteractionResult.PASS);
		UseItemCallback.EVENT.register((player, level, hand) ->
				LinearCommon.holdsBackUse(player)
						? InteractionResultHolder.fail(player.getItemInHand(hand))
						: InteractionResultHolder.pass(ItemStack.EMPTY));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				LinearCommon.registerCommands(dispatcher));

		ServerTickEvents.END_SERVER_TICK.register(LinearCommon::onServerTick);
	}
}
