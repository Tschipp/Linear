package tschipp.linear.common.caps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.GameType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import tschipp.linear.common.helper.BuildMode;

public class BuildDataStorage implements IStorage<IBuildData>
{

	@Override
	public NBTBase writeNBT(Capability<IBuildData> capability, IBuildData ins, EnumFacing side)
	{
		NBTTagCompound tag = new NBTTagCompound();

		if (ins.getCurrentBuildMode() != null)
			tag.setString("currentMode", ins.getCurrentBuildMode().getName());
		
		NBTTagList list = new NBTTagList();

		for (BuildMode mode : ins.getEnabledBuildModes())
			list.appendTag(new NBTTagString(mode.getName()));

		tag.setTag("enabledModes", list);
		
		tag.setBoolean("usingConfig", ins.isUsingConfig());
		tag.setDouble("placementRangeCreative", ins.getPlacementRange(GameType.CREATIVE));
		tag.setDouble("placementRangeSurvival", ins.getPlacementRange(GameType.SURVIVAL));
		tag.setBoolean("midairCreative", ins.canPlaceInMidair(GameType.CREATIVE));
		tag.setBoolean("midairSurvival", ins.canPlaceInMidair(GameType.SURVIVAL));
		tag.setInteger("maxBlocks", ins.getMaxBlocksPlaced());
		tag.setDouble("maxDistance", ins.getMaxDistance());

		return tag;
	}

	@Override
	public void readNBT(Capability<IBuildData> capability, IBuildData ins, EnumFacing side, NBTBase nbt)
	{
		NBTTagCompound tag = (NBTTagCompound) nbt;

		if(tag.hasKey("currentMode"))
			ins.setCurrentBuildMode(BuildMode.getByName(tag.getString("currentMode")));

		NBTTagList list = (NBTTagList) tag.getTag("enabledModes");
		BuildMode[] modes = new BuildMode[list.tagCount()];
		for(int i = 0; i < list.tagCount(); i++)
		{
			modes[i] = BuildMode.getByName(list.getStringTagAt(i));
		}
		
		ins.setEnabledBuildModes(modes);

		ins.setUsingConfig(tag.getBoolean("usingConfig"));
		ins.setPlacementRange(GameType.CREATIVE, tag.getDouble("placementRangeCreative"));
		ins.setPlacementRange(GameType.SURVIVAL, tag.getDouble("placementRangeSurvival"));
		ins.setPlaceInMidair(GameType.CREATIVE, tag.getBoolean("midairCreative"));
		ins.setPlaceInMidair(GameType.SURVIVAL, tag.getBoolean("midairSurvival"));
		ins.setMaxBlocksPlaced(tag.getInteger("maxBlocks"));
		ins.setMaxDistance(tag.getDouble("maxDistance"));
	}

}
