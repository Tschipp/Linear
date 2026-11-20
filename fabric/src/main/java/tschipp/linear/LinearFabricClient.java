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

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import tschipp.linear.client.LinePreview;
import tschipp.linear.client.LinearInput;
import tschipp.linear.client.ModeHud;
import tschipp.linear.client.keybinds.KeybindManager;

public class LinearFabricClient implements ClientModInitializer
{
	@Override
	public void onInitializeClient()
	{
		KeybindManager.ALL.forEach(KeyBindingHelper::registerKeyBinding);
		ClientTickEvents.END_CLIENT_TICK.register(LinearInput::onClientTick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> LinePreview.render(context.matrixStack(), context.camera()));
		HudRenderCallback.EVENT.register((graphics, deltaTracker) -> ModeHud.render(graphics));
	}
}
