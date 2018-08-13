package tschipp.linear.common.caps;

import net.minecraft.world.GameType;
import tschipp.linear.common.helper.BuildMode;

public interface IBuildData
{
	public BuildMode getCurrentBuildMode();
	
	public void setCurrentBuildMode(BuildMode mode);
	
	public BuildMode[] getEnabledBuildModes();
	
	public void setEnabledBuildModes(BuildMode[] modes);
	
	public boolean enableBuildMode(BuildMode mode);
	
	public boolean disableBuildMode(BuildMode mode);
	
	public int clearBuildModes();
	
	public int enableAllBuildModes();
	
	public boolean isUsingConfig();
	
	public void setUsingConfig(boolean bool);
	
	public double getPlacementRange(GameType mode);
	
	public void setPlacementRange(GameType mode, double range);
	
	public boolean canPlaceInMidair(GameType mode);
	
	public void setPlaceInMidair(GameType mode, boolean bool);
	
	public int getMaxBlocksPlaced();
	
	public void setMaxBlocksPlaced(int num);

	public double getMaxDistance();
	
	public void setMaxDistance(double num);
	
	public boolean isBuildingActivated();
	
	public void setBuildingActivated(boolean isActive);
	
}
