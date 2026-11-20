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

package tschipp.linear.build;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/** The shape a drag fills, see {@link BuildGeometry}. */
public enum BuildMode implements StringRepresentable
{
	LINE_3_AXES("line_3_axes"),
	LINE_2_AXES("line_2_axes"),
	LINE_1_AXIS("line_1_axis"),
	WALL_3_AXES("wall_3_axes"),
	WALL_2_AXES("wall_2_axes"),
	CIRCLE_2_AXES("circle_2_axes"),
	SPHERE_3_AXES("sphere_3_axes");

	public static final Codec<BuildMode> CODEC = StringRepresentable.fromEnum(BuildMode::values);
	public static final StreamCodec<ByteBuf, BuildMode> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], BuildMode::ordinal);

	private final String name;

	BuildMode(String name)
	{
		this.name = name;
	}

	@Override
	public String getSerializedName()
	{
		return name;
	}

	public String translationKey()
	{
		return "linear.mode." + name;
	}

	public static Optional<BuildMode> byName(String name)
	{
		return Arrays.stream(values()).filter(mode -> mode.name.equalsIgnoreCase(name)).findFirst();
	}

	/** The next enabled mode, wrapping around. */
	public BuildMode next(Collection<BuildMode> enabled)
	{
		BuildMode[] modes = values();
		for (int step = 1; step <= modes.length; step++)
		{
			BuildMode candidate = modes[(ordinal() + step) % modes.length];
			if (enabled.contains(candidate))
				return candidate;
		}
		return this;
	}
}
