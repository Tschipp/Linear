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

package tschipp.linear.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tschipp.linear.Constants;
import tschipp.linear.LinearCommon;
import tschipp.linear.client.LinePreview;
import tschipp.linear.client.LinearInput;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientEvents
{
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
			LinearInput.onClientTick(Minecraft.getInstance());
	}

	/** A click that starts or ends a line never reaches vanilla. */
	@SubscribeEvent
	public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event)
	{
		if (event.isUseItem() && Minecraft.getInstance().player != null && LinearCommon.holdsBackUse(Minecraft.getInstance().player))
		{
			event.setCanceled(true);
			event.setSwingHand(false);
		}
	}

	/** The camera rotation is already applied at this point, so start from an empty pose stack. */
	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event)
	{
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS)
			LinePreview.render(new PoseStack(), event.getCamera());
	}
}
