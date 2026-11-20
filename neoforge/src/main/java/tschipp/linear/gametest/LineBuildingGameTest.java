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

package tschipp.linear.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import tschipp.linear.Constants;
import tschipp.linear.LinearCommon;
import tschipp.linear.api.BlockSupply;
import tschipp.linear.api.HeldItemHandler;
import tschipp.linear.api.LinearApi;
import tschipp.linear.build.BuildMode;
import tschipp.linear.build.LineBuilder;
import tschipp.linear.build.LinearState;
import tschipp.linear.datagen.ModStructureProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Building lines on the server: what gets placed, what gets used up, and what gets refused. Uses a
 * hand-made survival player, since {@code makeMockServerPlayerInLevel} is always creative.
 */
@GameTestHolder(Constants.MOD_ID)
@PrefixGameTestTemplate(false)
public class LineBuildingGameTest
{
	private static final Vec3 HIT = new Vec3(0.5, 0, 0.5);

	/** A supply of sponges, which no other test uses. */
	private static final StockedSupply SPONGES = new StockedSupply(Items.SPONGE);
	/** A stick that hands out {@link HandingStick#block}. */
	private static final HandingStick STICK = new HandingStick();
	/** Makes glass placement get cancelled, like a protection mod would. */
	private static boolean refuseGlass;

	static
	{
		LinearApi.registerSupply(SPONGES);
		LinearApi.registerHeldItem(STICK);
		NeoForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) ->
		{
			if (refuseGlass && event.getPlacedBlock().is(Blocks.GLASS))
				event.setCanceled(true);
		});
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aLineGoesWhereTheModeSays(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 64));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(6, 1, 1));

		if (placed != 6)
			helper.fail("a line from x=1 to x=6 is six blocks, placed " + placed);
		for (int x = 1; x <= 6; x++)
			helper.assertBlockPresent(Blocks.STONE, new BlockPos(x, 1, 1));
		expectHeld(helper, player, Items.STONE, 58);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aCircleGoesAroundTheStart(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 64));
		LinearState.data(player).modes = Optional.of(EnumSet.of(BuildMode.CIRCLE_2_AXES));
		LinearState.data(player).mode = BuildMode.CIRCLE_2_AXES;

		int placed = build(helper, player, new BlockPos(4, 1, 4), new BlockPos(6, 1, 4));

		if (placed != 12)
			helper.fail("a circle of radius 2 is twelve blocks, placed " + placed);
		for (BlockPos pos : List.of(new BlockPos(6, 1, 4), new BlockPos(2, 1, 4), new BlockPos(4, 1, 6), new BlockPos(4, 1, 2),
				new BlockPos(6, 1, 5), new BlockPos(5, 1, 6)))
			helper.assertBlockPresent(Blocks.STONE, pos);
		helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(4, 1, 4));
		helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(5, 1, 5));
		expectHeld(helper, player, Items.STONE, 52);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aSphereIsHollow(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 64));
		LinearState.data(player).modes = Optional.of(EnumSet.of(BuildMode.SPHERE_3_AXES));
		LinearState.data(player).mode = BuildMode.SPHERE_3_AXES;

		int placed = build(helper, player, new BlockPos(4, 5, 4), new BlockPos(4, 6, 4));

		if (placed != 18)
			helper.fail("a sphere of radius 1 is eighteen blocks, placed " + placed);
		helper.assertBlockPresent(Blocks.STONE, new BlockPos(4, 6, 4));
		helper.assertBlockPresent(Blocks.STONE, new BlockPos(5, 5, 5));
		helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(4, 5, 4));
		helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(5, 6, 5));
		expectHeld(helper, player, Items.STONE, 46);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void theLineStopsWhenTheBlocksRunOut(GameTestHelper helper)
	{
		// Three in hand and one in the inventory: four of six positions get a block.
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 3));
		player.getInventory().setItem(20, new ItemStack(Items.STONE, 1));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(6, 1, 1));

		if (placed != 4)
			helper.fail("three in hand and one in the inventory are four blocks, placed " + placed);
		if (!player.getMainHandItem().isEmpty() || !player.getInventory().getItem(20).isEmpty())
			helper.fail("every stone should have been spent");
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void blacklistedBlocksDrawNothing(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.CHEST, 16));

		if (build(helper, player, new BlockPos(1, 1, 1), new BlockPos(6, 1, 1)) != 0)
			helper.fail("chests are on #linear:blacklist and must not be placed in a line");
		helper.assertBlockNotPresent(Blocks.CHEST, new BlockPos(1, 1, 1));
		expectHeld(helper, player, Items.CHEST, 16);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void anEndOutOfReachIsRefused(GameTestHelper helper)
	{
		// The line ends 30 blocks away, survival reach is 5.
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 64));

		if (build(helper, player, new BlockPos(1, 1, 1), new BlockPos(30, 1, 1)) != 0)
			helper.fail("a line ending out of reach must be refused outright");
		helper.assertBlockNotPresent(Blocks.STONE, new BlockPos(1, 1, 1));
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aModeThePlayerDoesNotHaveBuildsNothing(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 64));
		LinearState.data(player).modes = Optional.of(EnumSet.of(BuildMode.LINE_1_AXIS));
		LinearState.data(player).mode = BuildMode.LINE_3_AXES;

		if (build(helper, player, new BlockPos(1, 1, 1), new BlockPos(6, 1, 1)) != 0)
			helper.fail("the player only has line_1_axis; a line_3_axes request must be refused");
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aSupplyPaysWhileTheLastBlockStaysInHand(GameTestHelper helper)
	{
		SPONGES.stock = 5;
		ServerPlayer player = builder(helper, new ItemStack(Items.SPONGE, 1));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(4, 1, 1));

		if (placed != 4 || SPONGES.stock != 1)
			helper.fail("the supply should pay for all four sponges, placed " + placed + " with " + SPONGES.stock
					+ " left in the supply");
		expectHeld(helper, player, Items.SPONGE, 1);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void theLastBlockInHandGoesOnlyWhenNothingElseIsLeft(GameTestHelper helper)
	{
		// One in hand, one in the inventory, one in the supply. The one in hand goes last.
		SPONGES.stock = 1;
		ServerPlayer player = builder(helper, new ItemStack(Items.SPONGE, 1));
		player.getInventory().setItem(20, new ItemStack(Items.SPONGE, 1));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(4, 1, 1));

		if (placed != 3)
			helper.fail("three sponges in all should place three, placed " + placed);
		if (!player.getMainHandItem().isEmpty() || !player.getInventory().getItem(20).isEmpty() || SPONGES.stock != 0)
			helper.fail("with the line longer than the sponges, every one should be spent");
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void theInventoryPaysBeforeTheLastBlockInHand(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 1));
		player.getInventory().setItem(20, new ItemStack(Items.STONE, 2));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(2, 1, 1));

		if (placed != 2 || !player.getInventory().getItem(20).isEmpty())
			helper.fail("the two stone elsewhere in the inventory should pay for the line, placed " + placed);
		expectHeld(helper, player, Items.STONE, 1);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aHeldItemHandsOutItsOwnBlocks(GameTestHelper helper)
	{
		STICK.reset(Items.COBBLESTONE, 3);
		ServerPlayer player = builder(helper, new ItemStack(Items.STICK));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(5, 1, 1));

		if (placed != 3)
			helper.fail("the stick had three cobblestone to hand out, placed " + placed);
		helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(4, 1, 1));
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aBlockThatMayNotBePlacedIsGivenBack(GameTestHelper helper)
	{
		// Chests are blacklisted.
		STICK.reset(Items.CHEST, 10);
		ServerPlayer player = builder(helper, new ItemStack(Items.STICK));

		int placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(4, 1, 1));

		if (placed != 0 || STICK.givenBack != 4 || STICK.stock != 10)
			helper.fail("every chest should have gone back to the stick: placed " + placed + ", given back "
					+ STICK.givenBack + ", stock " + STICK.stock);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aRefusedPlacementKeepsTheBlock(GameTestHelper helper)
	{
		// A cancelled place event: nothing placed, nothing used up.
		ServerPlayer player = builder(helper, new ItemStack(Items.GLASS, 5));
		refuseGlass = true;
		int placed;
		try
		{
			placed = build(helper, player, new BlockPos(1, 1, 1), new BlockPos(3, 1, 1));
		}
		finally
		{
			refuseGlass = false;
		}

		if (placed != 0)
			helper.fail("every placement was cancelled, yet " + placed + " counted as placed");
		helper.assertBlockNotPresent(Blocks.GLASS, new BlockPos(1, 1, 1));
		expectHeld(helper, player, Items.GLASS, 5);
		helper.succeed();
	}

	@GameTest(template = ModStructureProvider.EMPTY_TEMPLATE, batch = "linear")
	public static void aSneakClickWithABlockIsHeldBack(GameTestHelper helper)
	{
		ServerPlayer player = builder(helper, new ItemStack(Items.STONE, 64));
		player.setShiftKeyDown(true);
		if (!LinearCommon.holdsBackUse(player))
			helper.fail("sneaking with a block and building on, a right-click starts a line instead");

		LinearState.data(player).building = false;
		if (LinearCommon.holdsBackUse(player))
			helper.fail("with building off, a right-click is an ordinary right-click");

		LinearState.data(player).building = true;
		player.setShiftKeyDown(false);
		if (LinearCommon.holdsBackUse(player))
			helper.fail("without sneaking, a right-click is an ordinary right-click");
		helper.succeed();
	}

	private static int build(GameTestHelper helper, ServerPlayer player, BlockPos start, BlockPos end)
	{
		return LineBuilder.build(player, helper.absolutePos(start), helper.absolutePos(end), InteractionHand.MAIN_HAND,
				Direction.UP, HIT);
	}

	/** A survival player in the middle of the test area, holding {@code held}. */
	private static ServerPlayer builder(GameTestHelper helper, ItemStack held)
	{
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "line-builder"), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
				cookie.clientInformation());
		// The place sound needs a connection.
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie);

		Vec3 standing = helper.absoluteVec(new Vec3(4.5, 1, 4.5));
		player.moveTo(standing.x, standing.y, standing.z);
		player.setItemInHand(InteractionHand.MAIN_HAND, held);
		return player;
	}

	private static void expectHeld(GameTestHelper helper, Player player, Item item, int count)
	{
		ItemStack held = player.getMainHandItem();
		if (!held.is(item) || held.getCount() != count)
			helper.fail("the player should still hold " + count + " " + item + ", holds " + held);
	}

	private static final class StockedSupply implements BlockSupply
	{
		private final Item item;
		private int stock;

		private StockedSupply(Item item)
		{
			this.item = item;
		}

		@Override
		public int available(Player player, ItemStack block, int wanted)
		{
			return block.is(item) ? Math.min(stock, wanted) : 0;
		}

		@Override
		public int take(ServerPlayer player, ItemStack block, int amount)
		{
			if (!block.is(item))
				return 0;
			int taken = Math.min(stock, amount);
			stock -= taken;
			return taken;
		}
	}

	private static final class HandingStick implements HeldItemHandler
	{
		private Block block = Blocks.AIR;
		private Item item = Items.AIR;
		private int stock;
		private int givenBack;

		private void reset(Item item, int stock)
		{
			this.item = item;
			this.block = Block.byItem(item);
			this.stock = stock;
			this.givenBack = 0;
		}

		@Override
		public boolean handles(ItemStack held)
		{
			return held.is(Items.STICK);
		}

		@Override
		public ItemStack preview(Player player, ItemStack held)
		{
			return new ItemStack(item);
		}

		@Override
		public int available(Player player, ItemStack held, int wanted)
		{
			return Math.min(stock, wanted);
		}

		@Override
		public ItemStack takeNext(ServerPlayer player, ItemStack held)
		{
			if (stock <= 0 || block == Blocks.AIR)
				return ItemStack.EMPTY;
			stock--;
			return new ItemStack(item);
		}

		@Override
		public void giveBack(ServerPlayer player, ItemStack held, ItemStack returned)
		{
			stock += returned.getCount();
			givenBack++;
		}
	}
}
