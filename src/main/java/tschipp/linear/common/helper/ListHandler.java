package tschipp.linear.common.helper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import tschipp.linear.common.config.LinearConfig;

public class ListHandler
{
	public static List<String> FORBIDDEN_TILES;
	public static List<String> ALLOWED_TILES;

	public static boolean isForbidden(Block block)
	{
		String name = block.getRegistryName().toString();
		if (FORBIDDEN_TILES.contains(name))
			return true;
		else
		{
			boolean contains = false;
			for (String s : FORBIDDEN_TILES)
			{
				if (s.contains("*"))
				{
					if(name.contains(s.replace("*", "")))
						contains = true;
				}
			}
			
			return contains;
		}
	}

	public static boolean isAllowed(Block block)
	{
		String name = block.getRegistryName().toString();
		if (ALLOWED_TILES.contains(name))
			return true;
		else
		{
			boolean contains = false;
			for (String s : ALLOWED_TILES)
			{
				if (s.contains("*"))
				{
					if(name.contains(s.replace("*", "")))
						contains = true;
				}
			}
			return contains;
		}

	}

	public static void initFilters()
	{
		String[] forbidden = LinearConfig.Blacklist.forbiddenBlocks;
		FORBIDDEN_TILES = new ArrayList<String>();

		for (int i = 0; i < forbidden.length; i++)
		{
			FORBIDDEN_TILES.add(forbidden[i]);
		}

		String[] allowedBlocks = LinearConfig.Whitelist.allowedBlocks;
		ALLOWED_TILES = new ArrayList<String>();
		for (int i = 0; i < allowedBlocks.length; i++)
		{
			ALLOWED_TILES.add(allowedBlocks[i]);
		}
	}

}
