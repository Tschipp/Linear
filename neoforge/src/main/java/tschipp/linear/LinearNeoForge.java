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

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import tschipp.linear.config.neoforge.ConfigLoaderImpl;
import tschipp.linear.data.LinearPlayerData;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class LinearNeoForge
{
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
			DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

	public static final Supplier<AttachmentType<LinearPlayerData>> PLAYER_DATA = ATTACHMENT_TYPES.register(
			Constants.PLAYER_DATA_ID.getPath(),
			() -> AttachmentType.builder(LinearPlayerData::new)
					.serialize(LinearPlayerData.CODEC)
					.copyOnDeath()
					.build());

	public LinearNeoForge(IEventBus modBus, ModContainer container)
	{
		LinearCommon.registerConfig();
		ConfigLoaderImpl.initialize(container);
		ATTACHMENT_TYPES.register(modBus);
		modBus.addListener(this::registerPackets);
	}

	private void registerPackets(final RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar("1");
		NeoForgePlatformHelperHolder.REGISTRAR = registrar;
		LinearCommon.registerServerPackets();
		LinearCommon.registerClientPackets();
		NeoForgePlatformHelperHolder.REGISTRAR = null;
	}

	/** Holds the registrar during RegisterPayloadHandlersEvent for the platform helper. */
	public static class NeoForgePlatformHelperHolder
	{
		public static PayloadRegistrar REGISTRAR;
	}
}
