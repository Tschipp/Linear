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

package tschipp.linear.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import tschipp.linear.Constants;
import tschipp.linear.build.BuildMode;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Per-player data: building on/off, the current mode, and overrides set with {@code /linear}. An empty
 * override means the server config value is used. Saved and kept on death.
 */
public class LinearPlayerData
{
	public static final Codec<LinearPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("building", true).forGetter(data -> data.building),
			BuildMode.CODEC.optionalFieldOf("mode", BuildMode.LINE_3_AXES).forGetter(data -> data.mode),
			BuildMode.CODEC.listOf().optionalFieldOf("modes").forGetter(data -> data.modes.map(List::copyOf)),
			Codec.INT.optionalFieldOf("maxBlocks").forGetter(data -> data.maxBlocks),
			Codec.DOUBLE.optionalFieldOf("maxDistance").forGetter(data -> data.maxDistance),
			Codec.DOUBLE.optionalFieldOf("reachSurvival").forGetter(data -> data.reachSurvival),
			Codec.DOUBLE.optionalFieldOf("reachCreative").forGetter(data -> data.reachCreative),
			Codec.BOOL.optionalFieldOf("midairSurvival").forGetter(data -> data.midairSurvival),
			Codec.BOOL.optionalFieldOf("midairCreative").forGetter(data -> data.midairCreative)
	).apply(instance, LinearPlayerData::new));

	public boolean building = true;
	public BuildMode mode = BuildMode.LINE_3_AXES;
	public Optional<Set<BuildMode>> modes = Optional.empty();
	public Optional<Integer> maxBlocks = Optional.empty();
	public Optional<Double> maxDistance = Optional.empty();
	public Optional<Double> reachSurvival = Optional.empty();
	public Optional<Double> reachCreative = Optional.empty();
	public Optional<Boolean> midairSurvival = Optional.empty();
	public Optional<Boolean> midairCreative = Optional.empty();

	public LinearPlayerData()
	{
	}

	private LinearPlayerData(boolean building, BuildMode mode, Optional<List<BuildMode>> modes, Optional<Integer> maxBlocks,
			Optional<Double> maxDistance, Optional<Double> reachSurvival, Optional<Double> reachCreative,
			Optional<Boolean> midairSurvival, Optional<Boolean> midairCreative)
	{
		this.building = building;
		this.mode = mode;
		this.modes = modes.map(LinearSettings::modeSet);
		this.maxBlocks = maxBlocks;
		this.maxDistance = maxDistance;
		this.reachSurvival = reachSurvival;
		this.reachCreative = reachCreative;
		this.midairSurvival = midairSurvival;
		this.midairCreative = midairCreative;
	}

	/** Resets the limits to the server config. Modes are reset separately. */
	public void resetLimits()
	{
		maxBlocks = Optional.empty();
		maxDistance = Optional.empty();
		reachSurvival = Optional.empty();
		reachCreative = Optional.empty();
		midairSurvival = Optional.empty();
		midairCreative = Optional.empty();
	}

	public void copyFrom(LinearPlayerData other)
	{
		building = other.building;
		mode = other.mode;
		modes = other.modes.map(LinearSettings::modeSet);
		maxBlocks = other.maxBlocks;
		maxDistance = other.maxDistance;
		reachSurvival = other.reachSurvival;
		reachCreative = other.reachCreative;
		midairSurvival = other.midairSurvival;
		midairCreative = other.midairCreative;
	}

	public CompoundTag toNbt()
	{
		Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this)
				.resultOrPartial(error -> Constants.LOG.error("Failed to write Linear player data: {}", error))
				.orElseGet(CompoundTag::new);
		return encoded instanceof CompoundTag tag ? tag : new CompoundTag();
	}

	public void readNbt(CompoundTag tag)
	{
		copyFrom(CODEC.parse(NbtOps.INSTANCE, tag)
				.resultOrPartial(error -> Constants.LOG.error("Failed to read Linear player data: {}", error))
				.orElseGet(LinearPlayerData::new));
	}
}
