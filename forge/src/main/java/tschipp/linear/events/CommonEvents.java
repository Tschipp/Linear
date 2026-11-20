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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.linear.Constants;
import tschipp.linear.LinearCommon;
import tschipp.linear.caps.LinearCapabilities;
import tschipp.linear.platform.Services;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class CommonEvents
{
	@SubscribeEvent
	public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Player)
			event.addCapability(Constants.PLAYER_DATA_ID, new LinearCapabilities.PlayerDataProvider());
	}

	/** Capabilities aren't copied automatically. */
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event)
	{
		Player original = event.getOriginal();
		original.reviveCaps();
		Services.PLATFORM.getPlayerData(event.getEntity()).copyFrom(Services.PLATFORM.getPlayerData(original));
		original.invalidateCaps();
	}

	/** A click that starts or ends a line does nothing else. */
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
	public static void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
			LinearCommon.onServerTick(event.getServer());
	}
}
