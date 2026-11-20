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

package tschipp.linear.datagen;

import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import tschipp.linear.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ModStructureProvider implements DataProvider
{
	public static final String EMPTY_TEMPLATE = "empty";
	public static final int SIZE = 9;

	private final PackOutput.PathProvider structures;

	public ModStructureProvider(PackOutput output)
	{
		this.structures = output.createPathProvider(PackOutput.Target.DATA_PACK, "structure");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache)
	{
		// save() gives the right format and DataVersion; the size is set below since it would be 0x0x0.
		CompoundTag tag = new StructureTemplate().save(new CompoundTag());
		ListTag size = new ListTag();
		size.add(IntTag.valueOf(SIZE));
		size.add(IntTag.valueOf(SIZE));
		size.add(IntTag.valueOf(SIZE));
		tag.put("size", size);

		Path file = structures.file(Constants.id(EMPTY_TEMPLATE), "nbt");
		return CompletableFuture.runAsync(() ->
		{
			try
			{
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				NbtIo.writeCompressed(tag, bytes);
				byte[] data = bytes.toByteArray();
				cache.writeIfNeeded(file, data, Hashing.sha1().hashBytes(data));
			}
			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}
		});
	}

	@Override
	public String getName()
	{
		return "Linear Game Test Structures";
	}
}
