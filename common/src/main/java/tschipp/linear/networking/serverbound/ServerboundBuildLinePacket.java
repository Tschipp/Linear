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

package tschipp.linear.networking.serverbound;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import tschipp.linear.Constants;
import tschipp.linear.build.LineBuilder;
import tschipp.linear.networking.PacketBase;

public record ServerboundBuildLinePacket(BlockPos start, BlockPos end, InteractionHand hand, Direction face,
		Vec3 hitOffset) implements PacketBase
{
	public static final Type<ServerboundBuildLinePacket> TYPE = new Type<>(Constants.PACKET_ID_BUILD_LINE);

	static final StreamCodec<ByteBuf, InteractionHand> HAND_CODEC =
			ByteBufCodecs.idMapper(id -> InteractionHand.values()[Math.floorMod(id, 2)], InteractionHand::ordinal);
	private static final StreamCodec<ByteBuf, Vec3> OFFSET_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, offset -> (float) offset.x,
			ByteBufCodecs.FLOAT, offset -> (float) offset.y,
			ByteBufCodecs.FLOAT, offset -> (float) offset.z,
			Vec3::new);

	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundBuildLinePacket> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, ServerboundBuildLinePacket::start,
			BlockPos.STREAM_CODEC, ServerboundBuildLinePacket::end,
			HAND_CODEC, ServerboundBuildLinePacket::hand,
			Direction.STREAM_CODEC, ServerboundBuildLinePacket::face,
			OFFSET_CODEC, ServerboundBuildLinePacket::hitOffset,
			ServerboundBuildLinePacket::new);

	@Override
	public Type<? extends PacketBase> type()
	{
		return TYPE;
	}

	@Override
	public void handle(Player player)
	{
		if (player instanceof ServerPlayer serverPlayer)
			LineBuilder.build(serverPlayer, start, end, hand, face, hitOffset);
	}
}
