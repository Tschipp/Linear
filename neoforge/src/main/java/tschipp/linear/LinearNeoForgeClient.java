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

package tschipp.linear;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import tschipp.linear.client.LinePreview;
import tschipp.linear.client.LinearInput;
import tschipp.linear.client.ModeHud;
import tschipp.linear.client.keybinds.KeybindManager;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class LinearNeoForgeClient
{
	@SubscribeEvent
	public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event)
	{
		KeybindManager.ALL.forEach(event::register);
	}

	@SubscribeEvent
	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event)
	{
		event.registerAbove(VanillaGuiLayers.HOTBAR, Constants.id("build_mode"), (graphics, deltaTracker) -> ModeHud.render(graphics));
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event)
	{
		LinearInput.onClientTick(Minecraft.getInstance());
	}

	/** The use key starting or ending a line never reaches vanilla, so nothing is predicted or sent. */
	@SubscribeEvent
	public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event)
	{
		if (event.isUseItem() && Minecraft.getInstance().player != null && LinearCommon.holdsBackUse(Minecraft.getInstance().player))
		{
			event.setCanceled(true);
			event.setSwingHand(false);
		}
	}

	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event)
	{
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
			LinePreview.render(event.getPoseStack(), event.getCamera());
	}
}
