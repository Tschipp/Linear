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

package tschipp.linear;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import tschipp.linear.build.BlockSource;
import tschipp.linear.build.LinearState;
import tschipp.linear.command.LinearCommand;
import tschipp.linear.config.ConfigLoader;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.data.LinearSettings;
import tschipp.linear.networking.clientbound.ClientboundModeUnlockedPacket;
import tschipp.linear.networking.clientbound.ClientboundSyncStatePacket;
import tschipp.linear.networking.serverbound.ServerboundBuildLinePacket;
import tschipp.linear.networking.serverbound.ServerboundSetBuildingPacket;
import tschipp.linear.networking.serverbound.ServerboundSetModePacket;
import tschipp.linear.platform.Services;

import java.util.Map;
import java.util.WeakHashMap;

public class LinearCommon
{
	/** What each player was last sent. A respawn creates a new player object, which then gets a fresh sync. */
	private static final Map<ServerPlayer, ClientboundSyncStatePacket> LAST_SENT = new WeakHashMap<>();

	public static void registerConfig()
	{
		ConfigLoader.registerConfig(Constants.SERVER_CONFIG);
		ConfigLoader.registerConfig(Constants.CLIENT_CONFIG);
	}

	public static void registerServerPackets()
	{
		Services.PLATFORM.registerServerboundPacket(ServerboundBuildLinePacket.TYPE, ServerboundBuildLinePacket.class,
				ServerboundBuildLinePacket.CODEC, ServerboundBuildLinePacket::handle);
		Services.PLATFORM.registerServerboundPacket(ServerboundSetModePacket.TYPE, ServerboundSetModePacket.class,
				ServerboundSetModePacket.CODEC, ServerboundSetModePacket::handle);
		Services.PLATFORM.registerServerboundPacket(ServerboundSetBuildingPacket.TYPE, ServerboundSetBuildingPacket.class,
				ServerboundSetBuildingPacket.CODEC, ServerboundSetBuildingPacket::handle);
	}

	public static void registerClientPackets()
	{
		Services.PLATFORM.registerClientboundPacket(ClientboundSyncStatePacket.TYPE, ClientboundSyncStatePacket.class,
				ClientboundSyncStatePacket.CODEC, ClientboundSyncStatePacket::handle);
		Services.PLATFORM.registerClientboundPacket(ClientboundModeUnlockedPacket.TYPE, ClientboundModeUnlockedPacket.class,
				ClientboundModeUnlockedPacket.CODEC, ClientboundModeUnlockedPacket::handle);
	}

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		LinearCommand.register(dispatcher);
	}

	/**
	 * Syncs each player's state when it changed, whatever changed it (keys, commands, respawn, config).
	 */
	public static void onServerTick(MinecraftServer server)
	{
		for (ServerPlayer player : server.getPlayerList().getPlayers())
		{
			LinearPlayerData data = LinearState.data(player);
			ClientboundSyncStatePacket state = new ClientboundSyncStatePacket(data.building, data.mode, LinearSettings.of(data));
			if (!state.equals(LAST_SENT.get(player)))
			{
				LAST_SENT.put(player, state);
				Services.PLATFORM.sendPacketToPlayer(state, player);
			}
		}
	}

	/** Sends the state next tick even if unchanged, to undo a client change the server refused. */
	public static void resync(ServerPlayer player)
	{
		LAST_SENT.remove(player);
	}

	/**
	 * Whether a right-click draws a line instead of its normal use: building on, sneaking, and holding
	 * something a line can be drawn with. Checked on both sides.
	 */
	public static boolean holdsBackUse(Player player)
	{
		return !player.isSpectator() && player.isShiftKeyDown() && LinearState.isBuilding(player)
				&& LinearState.settings(player).modes().contains(LinearState.mode(player))
				&& BlockSource.drawingHand(player) != null;
	}
}
