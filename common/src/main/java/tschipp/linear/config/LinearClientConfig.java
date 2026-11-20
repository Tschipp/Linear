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

@Config("linear-client")
public class LinearClientConfig
{
	@Property(type = PropertyType.CATEGORY, description = "Display settings")
	public Display display = new Display();

	@Category("display")
	public static class Display
	{
		@Property(type = PropertyType.BOOLEAN, description = "Hide the build mode indicator")
		public boolean hideIndicator = false;

		@Property(type = PropertyType.INT, min = 0, max = 10000, description = "Build mode indicator: distance from the left edge of the screen")
		public int indicatorX = 4;

		@Property(type = PropertyType.INT, min = 0, max = 10000, description = "Build mode indicator: distance from the bottom edge of the screen")
		public int indicatorY = 4;

		@Property(type = PropertyType.BOOLEAN, description = "Outline the end of a line in blue while dragging")
		public boolean endHighlight = true;
	}
}
