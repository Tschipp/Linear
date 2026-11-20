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

package tschipp.linear.networking.clientbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import tschipp.linear.Constants;
import tschipp.linear.build.BuildMode;
import tschipp.linear.build.LinearState;
import tschipp.linear.data.LinearSettings;
import tschipp.linear.networking.PacketBase;

public record ClientboundSyncStatePacket(boolean building, BuildMode mode, LinearSettings settings) implements PacketBase
{
	public static final Type<ClientboundSyncStatePacket> TYPE = new Type<>(Constants.PACKET_ID_SYNC_STATE);

	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncStatePacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ClientboundSyncStatePacket::building,
			BuildMode.STREAM_CODEC, ClientboundSyncStatePacket::mode,
			LinearSettings.STREAM_CODEC, ClientboundSyncStatePacket::settings,
			ClientboundSyncStatePacket::new);

	@Override
	public Type<? extends PacketBase> type()
	{
		return TYPE;
	}

	@Override
	public void handle(Player player)
	{
		LinearState.Client.update(building, mode, settings);
	}
}
