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

package tschipp.linear.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import tschipp.linear.Constants;
import tschipp.linear.build.BlockSource;
import tschipp.linear.build.BuildMode;
import tschipp.linear.build.LineBuilder;
import tschipp.linear.build.LinearState;
import tschipp.linear.data.LinearSettings;

import java.util.List;
import java.util.Objects;

/**
 * Shows the line being dragged as ghost blocks, tinted red where they won't be placed (out of blocks,
 * out of range or over the limit) and blue on the end. The block count is only updated when the line
 * changes or once a second, since it can be expensive (e.g. a bag that has to craft).
 */
public final class LinePreview
{
	private static final float GHOST_ALPHA = 0.5F;
	private static final float TINT_ALPHA = 0.45F;
	private static final long RECOUNT_TICKS = 20;

	@Nullable
	private static Key key;
	private static List<BlockPos> positions = List.of();
	private static List<BlockPos> refused = List.of();
	private static int available;
	private static long countedAt;
	@Nullable
	private static BlockState ghost;
	private static ItemStack icon = ItemStack.EMPTY;

	private LinePreview()
	{
	}

	/** Called from each loader's world render hook, after translucent blocks. */
	public static void render(PoseStack poseStack, Camera camera)
	{
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		LinearState.Drag drag = LinearState.Client.drag;
		if (player == null || minecraft.level == null || drag == null)
		{
			key = null;
			return;
		}

		BlockSource source = BlockSource.of(player, drag.hand());
		if (source == null)
			return;

		LinearSettings settings = LinearState.Client.settings();
		BlockHitResult end = LineBuilder.target(player, settings.reach(player), settings.midair(player));
		BlockPos endPos = end == null ? drag.start() : end.getBlockPos();

		ItemStack held = player.getItemInHand(drag.hand());
		Key current = new Key(drag.start(), endPos, LinearState.Client.mode, held.getItem(), held.getCount());
		long now = minecraft.level.getGameTime();
		if (!current.equals(key) || now - countedAt >= RECOUNT_TICKS)
		{
			key = current;
			countedAt = now;
			LineBuilder.Plan plan = LineBuilder.plan(minecraft.level, player, settings, LinearState.Client.mode, drag.start(), endPos);
			positions = plan.allowed();
			refused = plan.refused();
			available = Math.min(source.available(player, positions.size()), positions.size());
			icon = source.preview(player);
			ghost = ghostState(player, icon, drag);
		}

		Vec3 cameraPos = camera.getPosition();
		MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
		poseStack.pushPose();
		poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

		// Every position gets the ghost, so you can see what is being placed; red and blue go on top.
		if (ghost != null)
		{
			BlockRenderDispatcher blocks = minecraft.getBlockRenderer();
			MultiBufferSource translucent = type -> new FadedConsumer(buffers.getBuffer(Sheets.translucentCullBlockSheet()), GHOST_ALPHA);
			for (BlockPos pos : positions)
				renderGhost(blocks, poseStack, translucent, minecraft, pos);
			for (BlockPos pos : refused)
				renderGhost(blocks, poseStack, translucent, minecraft, pos);
			buffers.endBatch(Sheets.translucentCullBlockSheet());
		}

		// The end gets blue instead of red.
		boolean endHighlight = Constants.CLIENT_CONFIG.display.endHighlight;
		VertexConsumer fill = buffers.getBuffer(RenderType.debugFilledBox());
		if (endHighlight)
			tint(poseStack, fill, endPos, 0.3F, 0.4F, 1F);
		for (int i = available; i < positions.size(); i++)
			if (!endHighlight || !positions.get(i).equals(endPos))
				tint(poseStack, fill, positions.get(i), 1F, 0.25F, 0.25F);
		for (BlockPos pos : refused)
			if (!endHighlight || !pos.equals(endPos))
				tint(poseStack, fill, pos, 1F, 0.25F, 0.25F);
		buffers.endBatch(RenderType.debugFilledBox());

		poseStack.popPose();
	}

	private static void renderGhost(BlockRenderDispatcher blocks, PoseStack poseStack, MultiBufferSource translucent,
			Minecraft minecraft, BlockPos pos)
	{
		poseStack.pushPose();
		poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
		blocks.renderSingleBlock(ghost, poseStack, translucent, LevelRenderer.getLightColor(minecraft.level, pos),
				OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
	}

	/** A see-through coloured cube over the block. */
	private static void tint(PoseStack poseStack, VertexConsumer fill, BlockPos pos, float red, float green, float blue)
	{
		LevelRenderer.addChainedFilledBoxVertices(poseStack, fill, pos.getX() - 0.002, pos.getY() - 0.002, pos.getZ() - 0.002,
				pos.getX() + 1.002, pos.getY() + 1.002, pos.getZ() + 1.002, red, green, blue, TINT_ALPHA);
	}

	/**
	 * The block shown next to the crosshair and how many of it the line uses, or null when not dragging.
	 * {@code incomplete} if some positions won't get a block.
	 */
	@Nullable
	public static Counter counter()
	{
		if (key == null || LinearState.Client.drag == null || icon.isEmpty())
			return null;
		return new Counter(icon, available, available < positions.size() || !refused.isEmpty());
	}

	public record Counter(ItemStack icon, int count, boolean incomplete)
	{
	}

	/** The block as the first click would place it, so stairs and logs face the right way. */
	@Nullable
	private static BlockState ghostState(LocalPlayer player, ItemStack preview, LinearState.Drag drag)
	{
		if (!(preview.getItem() instanceof BlockItem blockItem))
			return null;

		BlockHitResult hit = new BlockHitResult(Vec3.atLowerCornerOf(drag.start()).add(drag.hitOffset()), drag.face(), drag.start(), false);
		BlockState state = blockItem.getBlock().getStateForPlacement(new BlockPlaceContext(player, drag.hand(), preview, hit));
		return state != null ? state : blockItem.getBlock().defaultBlockState();
	}

	/** What the cached line was worked out for. */
	private record Key(BlockPos start, BlockPos end, BuildMode mode, Object item, int count)
	{
		@Override
		public boolean equals(Object other)
		{
			return other instanceof Key key && start.equals(key.start) && end.equals(key.end) && mode == key.mode
					&& item == key.item && count == key.count;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(start, end, mode, System.identityHashCode(item), count);
		}
	}

	/** Lowers the alpha so the block is see-through. */
	private record FadedConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer
	{
		@Override
		public VertexConsumer addVertex(float x, float y, float z)
		{
			delegate.addVertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alphaValue)
		{
			delegate.setColor(red, green, blue, (int) (alphaValue * alpha));
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v)
		{
			delegate.setUv(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v)
		{
			delegate.setUv1(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v)
		{
			delegate.setUv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z)
		{
			delegate.setNormal(x, y, z);
			return this;
		}
	}
}
