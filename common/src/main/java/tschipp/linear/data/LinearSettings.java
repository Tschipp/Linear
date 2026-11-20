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

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import tschipp.linear.Constants;
import tschipp.linear.build.BuildMode;
import tschipp.linear.config.LinearServerConfig;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The limits for one player: the server config plus their overrides. Also sent to the player, so the
 * preview matches what the server builds.
 */
public record LinearSettings(Set<BuildMode> modes, int maxBlocks, double maxDistance, double reachSurvival,
		double reachCreative, boolean midairSurvival, boolean midairCreative, boolean useWhitelist)
{
	private static final StreamCodec<ByteBuf, List<BuildMode>> MODES_CODEC = BuildMode.STREAM_CODEC.apply(ByteBufCodecs.list());

	public static final StreamCodec<ByteBuf, LinearSettings> STREAM_CODEC = StreamCodec.of(
			(buffer, settings) ->
			{
				MODES_CODEC.encode(buffer, List.copyOf(settings.modes));
				ByteBufCodecs.VAR_INT.encode(buffer, settings.maxBlocks);
				ByteBufCodecs.DOUBLE.encode(buffer, settings.maxDistance);
				ByteBufCodecs.DOUBLE.encode(buffer, settings.reachSurvival);
				ByteBufCodecs.DOUBLE.encode(buffer, settings.reachCreative);
				ByteBufCodecs.BOOL.encode(buffer, settings.midairSurvival);
				ByteBufCodecs.BOOL.encode(buffer, settings.midairCreative);
				ByteBufCodecs.BOOL.encode(buffer, settings.useWhitelist);
			},
			buffer -> new LinearSettings(
					modeSet(MODES_CODEC.decode(buffer)),
					ByteBufCodecs.VAR_INT.decode(buffer),
					ByteBufCodecs.DOUBLE.decode(buffer),
					ByteBufCodecs.DOUBLE.decode(buffer),
					ByteBufCodecs.DOUBLE.decode(buffer),
					ByteBufCodecs.BOOL.decode(buffer),
					ByteBufCodecs.BOOL.decode(buffer),
					ByteBufCodecs.BOOL.decode(buffer)));

	/** The config defaults, used until the server sends the real values. */
	public static LinearSettings defaults()
	{
		return of(new LinearPlayerData());
	}

	public static LinearSettings of(LinearPlayerData data)
	{
		LinearServerConfig.Building config = Constants.SERVER_CONFIG.building;
		return new LinearSettings(
				data.modes.map(LinearSettings::modeSet).orElseGet(LinearSettings::configModes),
				data.maxBlocks.orElse(config.maxBlocks),
				data.maxDistance.orElse(config.maxDistance),
				data.reachSurvival.orElse(config.reachSurvival),
				data.reachCreative.orElse(config.reachCreative),
				data.midairSurvival.orElse(config.midairSurvival),
				data.midairCreative.orElse(config.midairCreative),
				config.useWhitelist);
	}

	/** The modes enabled in the config. Unknown names are logged and skipped. */
	public static Set<BuildMode> configModes()
	{
		List<BuildMode> modes = new ArrayList<>();
		for (String name : Constants.SERVER_CONFIG.building.enabledModes)
			BuildMode.byName(name).ifPresentOrElse(modes::add,
					() -> Constants.LOG.warn("Unknown build mode '{}' in the Linear config", name));
		return modeSet(modes);
	}

	/** {@code EnumSet.copyOf} that also works for an empty collection. */
	public static Set<BuildMode> modeSet(java.util.Collection<BuildMode> modes)
	{
		return modes.isEmpty() ? EnumSet.noneOf(BuildMode.class) : EnumSet.copyOf(modes);
	}

	public double reach(Player player)
	{
		return player.isCreative() ? reachCreative : reachSurvival;
	}

	public boolean midair(Player player)
	{
		return player.isCreative() ? midairCreative : midairSurvival;
	}
}
