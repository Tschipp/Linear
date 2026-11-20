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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import tschipp.linear.build.BuildMode;

/** "Build mode unlocked!" toast, shown after {@code /linear mode enable}. */
public class ModeUnlockedToast implements Toast
{
	private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("toast/advancement");
	private static final long DURATION_MS = 5000;
	private static final ItemStack ICON = new ItemStack(Items.BRICKS);

	private final BuildMode mode;

	public ModeUnlockedToast(BuildMode mode)
	{
		this.mode = mode;
	}

	@Override
	public Visibility render(GuiGraphics graphics, ToastComponent toasts, long timeSinceLastVisible)
	{
		graphics.blitSprite(BACKGROUND, 0, 0, width(), height());
		graphics.drawString(toasts.getMinecraft().font, Component.translatable("linear.toast.unlocked"), 30, 7, 0xFFFF00, false);
		graphics.drawString(toasts.getMinecraft().font, Component.translatable(mode.translationKey()), 30, 18, 0xFFFFFF, false);
		graphics.renderFakeItem(ICON, 8, 8);
		return timeSinceLastVisible >= DURATION_MS * toasts.getNotificationDisplayTimeMultiplier() ? Visibility.HIDE : Visibility.SHOW;
	}
}
