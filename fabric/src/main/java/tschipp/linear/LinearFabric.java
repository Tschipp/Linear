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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import tschipp.linear.config.fabric.ConfigLoaderImpl;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.events.CommonEvents;

import java.io.IOException;

public class LinearFabric implements ModInitializer
{
	/** copyOnDeath covers respawns only; dimension changes go through ServerPlayerEvents.COPY_FROM. */
	public static final AttachmentType<LinearPlayerData> PLAYER_DATA = AttachmentRegistry.create(
			Constants.PLAYER_DATA_ID,
			builder -> builder
					.initializer(LinearPlayerData::new)
					.persistent(LinearPlayerData.CODEC)
					.copyOnDeath());

	@Override
	public void onInitialize()
	{
		LinearCommon.registerConfig();
		try
		{
			ConfigLoaderImpl.initialize();
		}
		catch (IOException e)
		{
			Constants.LOG.error("Failed to load config", e);
		}

		LinearCommon.registerServerPackets();
		LinearCommon.registerClientPackets();
		CommonEvents.register();
	}
}
