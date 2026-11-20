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

package tschipp.linear.config;

import tschipp.linear.config.annotations.Category;
import tschipp.linear.config.annotations.Config;
import tschipp.linear.config.annotations.Property;

/**
 * Defaults for every player; {@code /linear} can override them per player. Not synced as a config, the
 * server sends each player their own values.
 */
@Config("linear-server")
public class LinearServerConfig
{
	@Property(type = PropertyType.CATEGORY, description = "Building settings")
	public Building building = new Building();

	@Category("building")
	public static class Building
	{
		@Property(type = PropertyType.INT, min = 1, max = 4096, description = "Most blocks placed in one line (survival only)")
		public int maxBlocks = 128;

		@Property(type = PropertyType.DOUBLE, minD = 1, maxD = 256, description = "Furthest a placed block may be from the player (survival only)")
		public double maxDistance = 20;

		@Property(type = PropertyType.DOUBLE, minD = 1, maxD = 64, description = "How far away a line can be started or ended, in survival")
		public double reachSurvival = 5;

		@Property(type = PropertyType.DOUBLE, minD = 1, maxD = 64, description = "How far away a line can be started or ended, in creative")
		public double reachCreative = 16;

		@Property(type = PropertyType.BOOLEAN, description = "Whether a line can end in midair, in survival")
		public boolean midairSurvival = false;

		@Property(type = PropertyType.BOOLEAN, description = "Whether a line can end in midair, in creative")
		public boolean midairCreative = true;

		@Property(type = PropertyType.STRING_ARRAY, description = "Build modes every player has. Others can be given per player with /linear mode enable. Possible: line_3_axes, line_2_axes, line_1_axis, wall_3_axes, wall_2_axes, circle_2_axes, sphere_3_axes")
		public String[] enabledModes = {"line_3_axes", "line_2_axes", "line_1_axis", "wall_3_axes", "wall_2_axes", "circle_2_axes",
				"sphere_3_axes"};

		@Property(type = PropertyType.BOOLEAN, description = "Only allow blocks in the #linear:whitelist item tag, instead of all but those in #linear:blacklist")
		public boolean useWhitelist = false;
	}
}
