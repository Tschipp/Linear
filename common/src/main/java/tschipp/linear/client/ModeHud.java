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

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import tschipp.linear.Constants;
import tschipp.linear.build.BlockSource;
import tschipp.linear.build.LinearState;
import tschipp.linear.config.LinearClientConfig;

/**
 * Shows the build mode and whether building is on while holding something that can draw lines, and the
 * block count next to the crosshair while dragging.
 */
public final class ModeHud
{
	private ModeHud()
	{
	}

	public static void render(GuiGraphics graphics)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.options.hideGui)
			return;

		renderCounter(graphics, minecraft);
		renderIndicator(graphics, minecraft);
	}

	/** The block's icon and "x N" just right of the crosshair, red if the line can't be finished. */
	private static void renderCounter(GuiGraphics graphics, Minecraft minecraft)
	{
		LinePreview.Counter counter = LinePreview.counter();
		if (counter == null || minecraft.screen != null)
			return;

		int x = graphics.guiWidth() / 2 + 6;
		int y = graphics.guiHeight() / 2;
		graphics.renderItem(counter.icon(), x, y - 8);
		graphics.drawString(minecraft.font, "x " + counter.count(), x + 18, y - minecraft.font.lineHeight / 2,
				counter.incomplete() ? 0xFF5555 : 0xFFFFFF);
	}

	private static void renderIndicator(GuiGraphics graphics, Minecraft minecraft)
	{
		LocalPlayer player = minecraft.player;
		LinearClientConfig.Display config = Constants.CLIENT_CONFIG.display;
		if (config.hideIndicator || BlockSource.drawingHand(player) == null)
			return;
		// The chat input covers the bottom-left corner.
		if (minecraft.screen instanceof ChatScreen)
			return;

		Font font = minecraft.font;
		boolean building = LinearState.Client.building;
		Component mode = Component.translatable("linear.hud.mode", Component.translatable(LinearState.Client.mode.translationKey()));
		Component state = Component.translatable("linear.hud.building",
				Component.translatable(building ? "linear.hud.on" : "linear.hud.off")
						.withStyle(building ? ChatFormatting.GREEN : ChatFormatting.RED));

		int x = config.indicatorX;
		int y = graphics.guiHeight() - config.indicatorY - 2 * font.lineHeight - 2;
		graphics.drawString(font, mode, x, y, building ? 0xFFFFFF : 0xA0A0A0);
		graphics.drawString(font, state, x, y + font.lineHeight + 2, 0xFFFFFF);
	}
}
