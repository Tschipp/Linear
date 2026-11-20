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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tschipp.linear.config.LinearClientConfig;
import tschipp.linear.config.LinearServerConfig;

public class Constants
{
	public static final String MOD_ID = "linear";
	public static final String MOD_NAME = "Linear";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final LinearServerConfig SERVER_CONFIG = new LinearServerConfig();
	public static final LinearClientConfig CLIENT_CONFIG = new LinearClientConfig();

	/** Blocks that can never be placed in a line. */
	public static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, id("blacklist"));
	/** With {@code useWhitelist} on, the only blocks that can be placed in a line. */
	public static final TagKey<Item> WHITELIST = TagKey.create(Registries.ITEM, id("whitelist"));

	public static final ResourceLocation PLAYER_DATA_ID = id("player_data");

	public static final ResourceLocation PACKET_ID_BUILD_LINE = id("build_line");
	public static final ResourceLocation PACKET_ID_SET_MODE = id("set_mode");
	public static final ResourceLocation PACKET_ID_SET_BUILDING = id("set_building");
	public static final ResourceLocation PACKET_ID_SYNC_STATE = id("sync_state");
	public static final ResourceLocation PACKET_ID_MODE_UNLOCKED = id("mode_unlocked");

	public static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
