/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
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

package tschipp.linear.platform;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.PacketDistributor;
import tschipp.linear.LinearNeoForge;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.config.BuiltConfig;
import tschipp.linear.config.neoforge.ConfigLoaderImpl;
import tschipp.linear.networking.PacketBase;
import tschipp.linear.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;

public class NeoForgePlatformHelper implements IPlatformHelper
{
	@Override
	public String getPlatformName()
	{
		return "NeoForge";
	}

	@Override
	public boolean isModLoaded(String modId)
	{
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment()
	{
		return !FMLLoader.isProduction();
	}

	@Override
	public void registerConfig(BuiltConfig cfg)
	{
		ConfigLoaderImpl.registerConfig(cfg);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		LinearNeoForge.NeoForgePlatformHelperHolder.REGISTRAR.playToServer(
				type,
				(StreamCodec<RegistryFriendlyByteBuf, T>) codec,
				(packet, context) -> handler.accept(packet, context.player()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		LinearNeoForge.NeoForgePlatformHelperHolder.REGISTRAR.playToClient(
				type,
				(StreamCodec<RegistryFriendlyByteBuf, T>) codec,
				(packet, context) -> handler.accept(packet, context.player()));
	}

	@Override
	public void sendPacketToServer(PacketBase packet)
	{
		PacketDistributor.sendToServer(packet);
	}

	@Override
	public void sendPacketToPlayer(PacketBase packet, ServerPlayer player)
	{
		PacketDistributor.sendToPlayer(player, packet);
	}

	@Override
	public void sendPacketToPlayersTracking(PacketBase packet, Entity entity)
	{
		PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
	}

	@Override
	public LinearPlayerData getPlayerData(Player player)
	{
		return player.getData(LinearNeoForge.PLAYER_DATA);
	}
}
