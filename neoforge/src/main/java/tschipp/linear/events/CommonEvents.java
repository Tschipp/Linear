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

import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import tschipp.linear.Constants;
import tschipp.linear.LinearCommon;
import tschipp.linear.LinearNeoForge;

@EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonEvents
{
	/** copyOnDeath doesn't cover dimension changes, so copy here for both. */
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event)
	{
		event.getEntity().getData(LinearNeoForge.PLAYER_DATA).copyFrom(event.getOriginal().getData(LinearNeoForge.PLAYER_DATA));
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		if (LinearCommon.holdsBackUse(event.getEntity()))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
		}
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		if (LinearCommon.holdsBackUse(event.getEntity()))
		{
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
		}
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event)
	{
		LinearCommon.registerCommands(event.getDispatcher());
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event)
	{
		LinearCommon.onServerTick(event.getServer());
	}
}
