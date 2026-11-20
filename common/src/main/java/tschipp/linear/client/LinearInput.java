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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import tschipp.linear.LinearCommon;
import tschipp.linear.build.BlockSource;
import tschipp.linear.build.BuildMode;
import tschipp.linear.build.LineBuilder;
import tschipp.linear.build.LinearState;
import tschipp.linear.client.keybinds.KeybindManager;
import tschipp.linear.data.LinearSettings;
import tschipp.linear.networking.serverbound.ServerboundBuildLinePacket;
import tschipp.linear.networking.serverbound.ServerboundSetBuildingPacket;
import tschipp.linear.networking.serverbound.ServerboundSetModePacket;
import tschipp.linear.platform.Services;

/**
 * Drawing a line on the client: press use while sneaking to start, release to finish. Vanilla never
 * sees the click, see {@link LinearCommon#holdsBackUse}.
 */
public final class LinearInput
{
	private static boolean wasUseDown;

	private LinearInput()
	{
	}

	public static void onClientTick(Minecraft minecraft)
	{
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null)
		{
			// Left the world.
			LinearState.Client.reset();
			wasUseDown = false;
			return;
		}

		handleKeys(player);
		handleDrag(minecraft, player);
	}

	private static void handleKeys(LocalPlayer player)
	{
		while (KeybindManager.SWITCH_MODE.consumeClick())
		{
			BuildMode next = LinearState.Client.mode.next(LinearState.Client.settings().modes());
			if (next == LinearState.Client.mode)
				continue;

			// Set right away; the server corrects it if needed.
			LinearState.Client.mode = next;
			Services.PLATFORM.sendPacketToServer(new ServerboundSetModePacket(next));
			player.displayClientMessage(Component.translatable("linear.message.mode", Component.translatable(next.translationKey())), true);
		}

		while (KeybindManager.TOGGLE_BUILDING.consumeClick())
		{
			LinearState.Client.building = !LinearState.Client.building;
			Services.PLATFORM.sendPacketToServer(new ServerboundSetBuildingPacket(LinearState.Client.building));
			player.displayClientMessage(Component.translatable(LinearState.Client.building ? "linear.message.on" : "linear.message.off"), true);
		}
	}

	private static void handleDrag(Minecraft minecraft, LocalPlayer player)
	{
		boolean useDown = minecraft.options.keyUse.isDown();
		InteractionHand hand = BlockSource.drawingHand(player);
		boolean active = minecraft.screen == null && LinearCommon.holdsBackUse(player);
		LinearSettings settings = LinearState.Client.settings();
		LinearState.Drag drag = LinearState.Client.drag;

		if (drag == null)
		{
			// Start a line.
			if (active && useDown && !wasUseDown)
			{
				BlockHitResult start = LineBuilder.target(player, settings.reach(player), false);
				if (start != null)
					LinearState.Client.drag = new LinearState.Drag(start.getBlockPos(), hand, start.getDirection(),
							LineBuilder.hitOffset(start));
			}
		}
		else if (!active || hand != drag.hand())
		{
			// Stopped sneaking, switched item or opened a screen: cancel the drag.
			LinearState.Client.drag = null;
		}
		else if (!useDown)
		{
			BlockHitResult end = LineBuilder.target(player, settings.reach(player), settings.midair(player));
			if (end != null)
			{
				Services.PLATFORM.sendPacketToServer(new ServerboundBuildLinePacket(drag.start(), end.getBlockPos(),
						drag.hand(), drag.face(), drag.hitOffset()));
				player.swing(drag.hand());
			}
			LinearState.Client.drag = null;
		}

		wasUseDown = useDown;
	}
}
