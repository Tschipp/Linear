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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.data.LinearSettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Places the blocks of a line on the server, and works out the positions for the preview. */
public final class LineBuilder
{
	/** Extra reach, in case the player moved a bit while the packet was on its way. */
	private static final double REACH_SLACK = 2;

	private LineBuilder()
	{
	}

	/**
	 * The positions that can take a block, nearest first, and in survival within the distance and block
	 * limits. Both sides.
	 */
	public static List<BlockPos> positions(Level level, Player player, LinearSettings settings, BuildMode mode,
			BlockPos start, BlockPos end)
	{
		return plan(level, player, settings, mode, start, end).allowed();
	}

	/**
	 * Splits a drag's free positions into the ones that may get a block (nearest first) and the ones the
	 * survival distance or block limits refuse.
	 */
	public static Plan plan(Level level, Player player, LinearSettings settings, BuildMode mode, BlockPos start,
			BlockPos end)
	{
		Vec3 eyes = player.getEyePosition();
		boolean limited = !player.isCreative();

		List<BlockPos> allowed = new ArrayList<>();
		List<BlockPos> refused = new ArrayList<>();
		for (BlockPos pos : BuildGeometry.positions(mode, start, end))
		{
			if (!level.isInWorldBounds(pos) || !level.isLoaded(pos) || !level.getBlockState(pos).canBeReplaced())
				continue;
			if (limited && Vec3.atCenterOf(pos).distanceTo(eyes) > settings.maxDistance())
				refused.add(pos);
			else
				allowed.add(pos);
		}

		allowed.sort(Comparator.comparingDouble(pos -> Vec3.atCenterOf(pos).distanceToSqr(eyes)));
		if (limited && allowed.size() > settings.maxBlocks())
		{
			refused.addAll(allowed.subList(settings.maxBlocks(), allowed.size()));
			allowed = new ArrayList<>(allowed.subList(0, settings.maxBlocks()));
		}
		return new Plan(allowed, refused);
	}

	/** See {@link #plan}. */
	public record Plan(List<BlockPos> allowed, List<BlockPos> refused)
	{
	}

	/**
	 * Builds a line the client asked for, after validating the request.
	 *
	 * @return how many blocks were placed
	 */
	public static int build(ServerPlayer player, BlockPos start, BlockPos end, InteractionHand hand, Direction face,
			Vec3 hitOffset)
	{
		LinearPlayerData data = LinearState.data(player);
		LinearSettings settings = LinearSettings.of(data);
		if (!data.building || !settings.modes().contains(data.mode) || player.isSpectator())
			return 0;

		BlockSource source = BlockSource.of(player, hand);
		if (source == null)
			return 0;

		double reach = settings.reach(player) + REACH_SLACK;
		if (!withinReach(player, start, reach) || !withinReach(player, end, reach))
			return 0;

		ServerLevel level = player.serverLevel();
		Vec3 offset = new Vec3(clamp(hitOffset.x), clamp(hitOffset.y), clamp(hitOffset.z));
		int placed = 0;

		for (BlockPos pos : positions(level, player, settings, data.mode, start, end))
		{
			if (!level.mayInteract(player, pos))
				continue;

			ItemStack block = source.takeOne(player);
			if (block.isEmpty())
				break;

			if (!BlockSource.allowed(block, settings) || !place(player, level, pos, hand, face, offset, block))
			{
				source.giveBack(player, block);
				continue;
			}

			placed++;
			playPlaceSound(player, level, pos);
		}

		if (placed > 0)
			player.swing(hand, true);
		return placed;
	}

	/**
	 * Places a block at {@code pos} as if the player clicked there like on the first block. Uses
	 * {@code ItemStack.useOn} so place events fire and protection mods can cancel it.
	 */
	private static boolean place(ServerPlayer player, ServerLevel level, BlockPos pos, InteractionHand hand,
			Direction face, Vec3 hitOffset, ItemStack block)
	{
		if (!player.mayUseItemAt(pos, face, block))
			return false;

		BlockHitResult hit = new BlockHitResult(Vec3.atLowerCornerOf(pos).add(hitOffset), face, pos, false);
		BlockPlaceContext context = new BlockPlaceContext(player, hand, block, hit);
		// If the spot got filled since, the context would place next to it instead.
		if (!context.getClickedPos().equals(pos) || !context.canPlace())
			return false;

		InteractionResult result = block.useOn(context);
		return result.consumesAction();
	}

	/** Vanilla doesn't send the place sound to the placer, whose client didn't predict it here. */
	private static void playPlaceSound(ServerPlayer player, ServerLevel level, BlockPos pos)
	{
		BlockState state = level.getBlockState(pos);
		SoundType sound = state.getSoundType();
		player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound.getPlaceSound()),
				SoundSource.BLOCKS, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				(sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, level.getRandom().nextLong()));
	}

	private static boolean withinReach(Player player, BlockPos pos, double reach)
	{
		return Vec3.atCenterOf(pos).distanceToSqr(player.getEyePosition()) <= reach * reach;
	}

	private static double clamp(double value)
	{
		return Math.max(0, Math.min(1, value));
	}

	/**
	 * Where a click builds: the clicked block if replaceable, else the one in front of it. If nothing
	 * was hit and midair is allowed, the block at the end of the player's reach. Both sides.
	 */
	@Nullable
	public static BlockHitResult target(Player player, double reach, boolean midair)
	{
		HitResult result = player.pick(reach, 1.0F, false);
		if (result instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK)
		{
			BlockPos clicked = hit.getBlockPos();
			BlockPos pos = player.level().getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(hit.getDirection());
			return new BlockHitResult(hit.getLocation(), hit.getDirection(), pos, false);
		}

		if (!midair)
			return null;

		Vec3 end = player.getEyePosition().add(player.getLookAngle().scale(reach));
		return new BlockHitResult(end, Direction.getNearest(player.getLookAngle()).getOpposite(), BlockPos.containing(end), false);
	}

	/**
	 * Where the click hit within the target block, 0 to 1 per axis. Every block uses the same offset, so
	 * e.g. slabs are all top or all bottom.
	 */
	public static Vec3 hitOffset(BlockHitResult target)
	{
		Vec3 offset = target.getLocation().subtract(Vec3.atLowerCornerOf(target.getBlockPos()));
		return new Vec3(clamp(offset.x), clamp(offset.y), clamp(offset.z));
	}
}
