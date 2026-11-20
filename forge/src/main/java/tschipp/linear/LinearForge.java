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

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;
import tschipp.linear.caps.LinearCapabilities;
import tschipp.linear.config.forge.ConfigLoaderImpl;

@Mod(Constants.MOD_ID)
public class LinearForge
{
	public static SimpleChannel network;

	public LinearForge(FMLJavaModLoadingContext context)
	{
		IEventBus modBus = context.getModEventBus();
		LinearCommon.registerConfig();
		ConfigLoaderImpl.initialize(modBus);
		modBus.addListener(this::setup);
		modBus.addListener(LinearCapabilities::register);
	}

	private void setup(final FMLCommonSetupEvent event)
	{
		network = ChannelBuilder.named(Constants.id("network")).simpleChannel();
		LinearCommon.registerServerPackets();
		LinearCommon.registerClientPackets();
	}
}
