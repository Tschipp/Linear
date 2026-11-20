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

package tschipp.linear.client.keybinds;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class KeybindManager
{
	public static final String CATEGORY = "key.linear.category";

	public static final KeyMapping SWITCH_MODE = new KeyMapping("key.linear.switch_mode", GLFW.GLFW_KEY_G, CATEGORY);
	public static final KeyMapping TOGGLE_BUILDING = new KeyMapping("key.linear.toggle_building", GLFW.GLFW_KEY_V, CATEGORY);

	public static final List<KeyMapping> ALL = List.of(SWITCH_MODE, TOGGLE_BUILDING);
}
