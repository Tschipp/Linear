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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;
import tschipp.linear.LinearCommonClient;
import tschipp.linear.LinearForge;
import tschipp.linear.caps.LinearCapabilities;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.config.BuiltConfig;
import tschipp.linear.config.forge.ConfigLoaderImpl;
import tschipp.linear.networking.PacketBase;
import tschipp.linear.platform.services.IPlatformHelper;

import java.util.function.BiConsumer;

public class ForgePlatformHelper implements IPlatformHelper
{
	@Override
	public String getPlatformName()
	{
		return "Forge";
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
		BiConsumer<T, CustomPayloadEvent.Context> serverHandler = (packet, ctx) -> {
			if (!ctx.isServerSide())
				return;

			ctx.setPacketHandled(true);
			ctx.enqueueWork(() -> handler.accept(packet, ctx.getSender()));
		};

		LinearForge.network.messageBuilder(clazz)
				.codec((StreamCodec<FriendlyByteBuf, T>) codec)
				.consumerMainThread(serverHandler)
				.add();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PacketBase, B extends FriendlyByteBuf> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<B, T> codec, BiConsumer<T, Player> handler)
	{
		BiConsumer<T, CustomPayloadEvent.Context> clientHandler = (packet, ctx) -> {
			if (!ctx.isClientSide())
				return;

			ctx.setPacketHandled(true);
			ctx.enqueueWork(() -> handler.accept(packet, LinearCommonClient.getPlayer()));
		};

		LinearForge.network.messageBuilder(clazz)
				.codec((StreamCodec<FriendlyByteBuf, T>) codec)
				.consumerMainThread(clientHandler)
				.add();
	}

	@Override
	public void sendPacketToServer(PacketBase packet)
	{
		LinearForge.network.send(packet, PacketDistributor.SERVER.noArg());
	}

	@Override
	public void sendPacketToPlayer(PacketBase packet, ServerPlayer player)
	{
		LinearForge.network.send(packet, PacketDistributor.PLAYER.with(player));
	}

	@Override
	public void sendPacketToPlayersTracking(PacketBase packet, Entity entity)
	{
		LinearForge.network.send(packet, PacketDistributor.TRACKING_ENTITY.with(entity));
	}

	/** Players without the capability (fake players) get a throwaway instance. */
	@Override
	public LinearPlayerData getPlayerData(Player player)
	{
		return player.getCapability(LinearCapabilities.PLAYER_DATA, null)
				.resolve()
				.orElseGet(LinearPlayerData::new);
	}
}
