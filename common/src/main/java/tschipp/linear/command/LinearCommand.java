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

package tschipp.linear.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import tschipp.linear.build.BuildMode;
import tschipp.linear.build.LinearState;
import tschipp.linear.data.LinearPlayerData;
import tschipp.linear.data.LinearSettings;
import tschipp.linear.networking.clientbound.ClientboundModeUnlockedPacket;
import tschipp.linear.platform.Services;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * {@code /linear}: per-player overrides of the server config. Works in datapack functions, so packs can
 * unlock build modes as progression.
 */
public final class LinearCommand
{
	private static final String ALL = "all";
	private static final DynamicCommandExceptionType UNKNOWN_MODE =
			new DynamicCommandExceptionType(name -> Component.translatable("commands.linear.unknown_mode", name));

	private LinearCommand()
	{
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("linear")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("mode")
						.then(Commands.literal("enable").then(Commands.argument("targets", EntityArgument.players())
								.then(modeArgument().executes(context -> changeModes(context, true)))))
						.then(Commands.literal("disable").then(Commands.argument("targets", EntityArgument.players())
								.then(modeArgument().executes(context -> changeModes(context, false)))))
						.then(Commands.literal("reset").then(Commands.argument("targets", EntityArgument.players())
								.executes(LinearCommand::resetModes))))
				.then(Commands.literal("limits")
						.then(Commands.literal("reset").then(Commands.argument("targets", EntityArgument.players())
								.executes(LinearCommand::resetLimits)))
						.then(Commands.argument("targets", EntityArgument.players())
								.then(Commands.literal("maxBlocks").then(Commands.argument("value", IntegerArgumentType.integer(1, 4096))
										.executes(context -> setLimit(context, "maxBlocks",
												data -> data.maxBlocks = Optional.of(IntegerArgumentType.getInteger(context, "value"))))))
								.then(Commands.literal("maxDistance").then(Commands.argument("value", DoubleArgumentType.doubleArg(1, 256))
										.executes(context -> setLimit(context, "maxDistance",
												data -> data.maxDistance = Optional.of(DoubleArgumentType.getDouble(context, "value"))))))
								.then(gameModeLimit("reach", DoubleArgumentType.doubleArg(1, 64),
										(data, context, creative) ->
										{
											double value = DoubleArgumentType.getDouble(context, "value");
											if (creative)
												data.reachCreative = Optional.of(value);
											else
												data.reachSurvival = Optional.of(value);
										}))
								.then(gameModeLimit("midair", BoolArgumentType.bool(),
										(data, context, creative) ->
										{
											boolean value = BoolArgumentType.getBool(context, "value");
											if (creative)
												data.midairCreative = Optional.of(value);
											else
												data.midairSurvival = Optional.of(value);
										}))))
				.then(Commands.literal("info").then(Commands.argument("target", EntityArgument.player())
						.executes(LinearCommand::info))));
	}

	private static ArgumentBuilder<CommandSourceStack, ?> modeArgument()
	{
		return Commands.argument("mode", StringArgumentType.word())
				.suggests((context, builder) -> SharedSuggestionProvider.suggest(
						Stream.concat(Arrays.stream(BuildMode.values()).map(BuildMode::getSerializedName), Stream.of(ALL)), builder));
	}

	@FunctionalInterface
	private interface GameModeSetter
	{
		void set(LinearPlayerData data, CommandContext<CommandSourceStack> context, boolean creative);
	}

	private static ArgumentBuilder<CommandSourceStack, ?> gameModeLimit(String name,
			com.mojang.brigadier.arguments.ArgumentType<?> type, GameModeSetter setter)
	{
		return Commands.literal(name)
				.then(Commands.literal("survival").then(Commands.argument("value", type)
						.executes(context -> setLimit(context, name, data -> setter.set(data, context, false)))))
				.then(Commands.literal("creative").then(Commands.argument("value", type)
						.executes(context -> setLimit(context, name, data -> setter.set(data, context, true)))));
	}

	private static Set<BuildMode> parseModes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		String name = StringArgumentType.getString(context, "mode");
		if (name.equalsIgnoreCase(ALL))
			return EnumSet.allOf(BuildMode.class);
		return EnumSet.of(BuildMode.byName(name).orElseThrow(() -> UNKNOWN_MODE.create(name)));
	}

	private static int changeModes(CommandContext<CommandSourceStack> context, boolean enable) throws CommandSyntaxException
	{
		Set<BuildMode> changed = parseModes(context);
		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");

		for (ServerPlayer player : players)
		{
			LinearPlayerData data = LinearState.data(player);
			Set<BuildMode> modes = EnumSet.noneOf(BuildMode.class);
			modes.addAll(LinearSettings.of(data).modes());

			for (BuildMode mode : changed)
			{
				if (enable && modes.add(mode))
					Services.PLATFORM.sendPacketToPlayer(new ClientboundModeUnlockedPacket(mode), player);
				else if (!enable)
					modes.remove(mode);
			}

			data.modes = Optional.of(modes);
			if (!modes.contains(data.mode))
				data.mode = data.mode.next(modes);
		}

		List<String> names = changed.stream().map(BuildMode::getSerializedName).toList();
		context.getSource().sendSuccess(() -> Component.translatable(enable ? "commands.linear.mode.enabled" : "commands.linear.mode.disabled",
				String.join(", ", names), players.size()), true);
		return players.size();
	}

	private static int resetModes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
		for (ServerPlayer player : players)
		{
			LinearPlayerData data = LinearState.data(player);
			data.modes = Optional.empty();
			Set<BuildMode> modes = LinearSettings.configModes();
			if (!modes.contains(data.mode))
				data.mode = data.mode.next(modes);
		}

		context.getSource().sendSuccess(() -> Component.translatable("commands.linear.mode.reset", players.size()), true);
		return players.size();
	}

	private static int setLimit(CommandContext<CommandSourceStack> context, String name,
			Consumer<LinearPlayerData> setter) throws CommandSyntaxException
	{
		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
		for (ServerPlayer player : players)
			setter.accept(LinearState.data(player));

		context.getSource().sendSuccess(() -> Component.translatable("commands.linear.limits.set", name, players.size()), true);
		return players.size();
	}

	private static int resetLimits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "targets");
		for (ServerPlayer player : players)
			LinearState.data(player).resetLimits();

		context.getSource().sendSuccess(() -> Component.translatable("commands.linear.limits.reset", players.size()), true);
		return players.size();
	}

	private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
	{
		ServerPlayer player = EntityArgument.getPlayer(context, "target");
		LinearPlayerData data = LinearState.data(player);
		LinearSettings settings = LinearSettings.of(data);

		String modes = settings.modes().stream().map(BuildMode::getSerializedName).reduce((a, b) -> a + ", " + b).orElse("-");
		context.getSource().sendSuccess(() -> Component.translatable("commands.linear.info",
				player.getDisplayName(), data.building, data.mode.getSerializedName(), modes, settings.maxBlocks(),
				settings.maxDistance(), settings.reachSurvival(), settings.reachCreative(), settings.midairSurvival(),
				settings.midairCreative()), false);
		return 1;
	}
}
