package tschipp.linear.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import tschipp.linear.common.caps.IBuildData;
import tschipp.linear.common.helper.BuildMode;
import tschipp.linear.common.helper.LinearHelper;

@ZenRegister
@ZenClass("mods.linear")
public class LinearCT
{
	private static IBuildData getBuildData(IPlayer player)
	{
		EntityPlayer eplayer = getPlayer(player);
		if (eplayer != null)
			return LinearHelper.getBuildData(eplayer);
		return null;
	}

	private static EntityPlayer getPlayer(IPlayer player)
	{
		return CraftTweakerMC.getPlayer(player);
	}

	@ZenMethod
	public static void enableBuildMode(IPlayer player, String mode)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			BuildMode buildMode = BuildMode.getByName(mode);
			if (buildMode != null)
			{
				data.enableBuildMode(buildMode);

				EntityPlayer p = getPlayer(player);
				if (p instanceof EntityPlayerMP)
					LinearHelper.syncBuildDataWithClient(p);
				
			}
		}
	}

	@ZenMethod
	public static void disableBuildMode(IPlayer player, String mode)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			BuildMode buildMode = BuildMode.getByName(mode);
			if (buildMode != null)
			{
				data.disableBuildMode(buildMode);

				EntityPlayer p = getPlayer(player);
				if (p instanceof EntityPlayerMP)
					LinearHelper.syncBuildDataWithClient(p);
			}
		}
	}

	@ZenMethod
	public static void clearBuildModes(IPlayer player)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			data.clearBuildModes();

			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
		
		}
	}
	
	@ZenMethod
	public static void enableAllBuildModes(IPlayer player)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			data.enableAllBuildModes();

			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
		
		}
	}

	@ZenMethod
	public static void setCurrentBuildMode(IPlayer player, String mode)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			BuildMode buildMode = BuildMode.getByName(mode);
			data.setCurrentBuildMode(buildMode);
			
			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
		
		}
	}

	@ZenMethod
	public static String getCurrentBuildMode(IPlayer player)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			return data.getCurrentBuildMode() == null ? "null" : data.getCurrentBuildMode().getName();
		}
		return "null";
	}

	@ZenMethod
	public static void setUsesConfigValues(IPlayer player, boolean uses)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			data.setUsingConfig(uses);
			
			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
			
		}
	}

	@ZenMethod
	public static void setPlacementRange(IPlayer player, String gamemode, double range)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			GameType mode = GameType.parseGameTypeWithDefault(gamemode.toLowerCase(), GameType.SURVIVAL);
			data.setPlacementRange(mode, range);
			data.setUsingConfig(false);
			
			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
		
		}
	}

	@ZenMethod
	public static double getPlacementRange(IPlayer player, String gamemode)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			GameType mode = GameType.parseGameTypeWithDefault(gamemode.toLowerCase(), GameType.SURVIVAL);
			return data.getPlacementRange(mode);
		}
		return 0;
	}

	@ZenMethod
	public static void setCanPlaceInMidair(IPlayer player, String gamemode, boolean canPlace)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			GameType mode = GameType.parseGameTypeWithDefault(gamemode.toLowerCase(), GameType.SURVIVAL);
			data.setPlaceInMidair(mode, canPlace);
			data.setUsingConfig(false);
			
			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
			
		}
	}

	@ZenMethod
	public static boolean getCanPlaceInMidair(IPlayer player, String gamemode)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			GameType mode = GameType.parseGameTypeWithDefault(gamemode.toLowerCase(), GameType.SURVIVAL);
			return data.canPlaceInMidair(mode);
		}
		return false;
	}

	@ZenMethod
	public static void setMaxBlocksPlaced(IPlayer player, int maxPlaced)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			data.setMaxBlocksPlaced(maxPlaced);
			data.setUsingConfig(false);
			
			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
			
		}
	}

	@ZenMethod
	public static int getMaxBlocksPlaced(IPlayer player)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			return data.getMaxBlocksPlaced();
		}
		return 0;
	}

	@ZenMethod
	public static void setMaxPlacementDistance(IPlayer player, double distance)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			data.setMaxDistance(distance);
			data.setUsingConfig(false);
			
			EntityPlayer p = getPlayer(player);
			if (p instanceof EntityPlayerMP)
				LinearHelper.syncBuildDataWithClient(p);
		
		}
	}

	@ZenMethod
	public static double getMaxPlacementDistance(IPlayer player)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			return data.getMaxDistance();
		}
		return 0;
	}

}
