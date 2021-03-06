package tschipp.linear.common.caps;

import javax.annotation.Nullable;

import net.minecraft.world.GameType;
import tschipp.linear.common.config.LinearConfig;
import tschipp.linear.common.helper.BuildMode;
import tschipp.linear.common.helper.LinearHelper;

public class BuildData implements IBuildData
{

	private BuildMode currentMode = BuildMode.LINE3;

	private BuildMode[] enabledModes = LinearHelper.getBuildModesFromConfig();

	private boolean isUsingConfig = true;

	private boolean midairCreative = LinearConfig.Settings.placeInMidairCreative;
	private boolean midairSurivival = LinearConfig.Settings.placeInMidairSurvival;

	private double rangeSurvival = LinearConfig.Settings.blockPlacementRangeSurvival;
	private double rangeCreative = LinearConfig.Settings.blockPlacementRangeCreative;

	private int maxBlocks = LinearConfig.Settings.maxBlocks;

	private double maxDistance = LinearConfig.Settings.maxDistance;
	
	private boolean isBuildingActive = true;

	@Nullable
	@Override
	public BuildMode getCurrentBuildMode()
	{
		return currentMode;
	}

	@Override
	public BuildMode[] getEnabledBuildModes()
	{
		return enabledModes;
	}

	@Override
	public boolean enableBuildMode(BuildMode mode)
	{
		boolean alreadyEnabled = false;
		for (BuildMode m : enabledModes)
			if (m == mode)
				alreadyEnabled = true;

		if (alreadyEnabled)
			return false;

		BuildMode[] modes = new BuildMode[enabledModes.length + 1];
		System.arraycopy(enabledModes, 0, modes, 0, enabledModes.length);
		modes[modes.length - 1] = mode;
		enabledModes = modes;

		if (enabledModes.length == 1)
			currentMode = enabledModes[0];
		
		return true;
	}

	@Override
	public boolean disableBuildMode(BuildMode mode)
	{
		if (enabledModes.length > 0)
		{
			BuildMode[] modes = new BuildMode[enabledModes.length - 1];
			int idx = -1;
			for (int i = 0; i < enabledModes.length; i++)
			{
				if (enabledModes[i] == mode)
					idx = i;
			}

			if (idx != -1)
			{
				System.arraycopy(enabledModes, 0, modes, 0, idx);
				System.arraycopy(enabledModes, idx + 1, modes, idx, modes.length - idx);

				enabledModes = modes;

				if (currentMode == mode)
				{
					if (enabledModes.length == 0)
						currentMode = null;
					else if (idx == enabledModes.length)
						currentMode = enabledModes[0];
					else
						currentMode = enabledModes[idx];
				}
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int clearBuildModes()
	{
		int disabledModes = enabledModes.length;
		enabledModes = new BuildMode[] {};
		currentMode = null;
		
		return disabledModes;
	}

	@Override
	public int enableAllBuildModes()
	{
		int enabledBuildModes = BuildMode.values().length - enabledModes.length;
		enabledModes = BuildMode.values();
		currentMode = BuildMode.values()[0];
		return enabledBuildModes;
	}

	@Override
	public void setCurrentBuildMode(BuildMode mode)
	{
		currentMode = mode;
	}

	@Override
	public void setEnabledBuildModes(BuildMode[] modes)
	{
		enabledModes = modes;
		if(enabledModes.length == 0)
		{
			currentMode = null;
			return;
		}
		
		boolean contained = false;
		for(BuildMode m : enabledModes)
		{
			if(m == currentMode)
				contained = true;
		}
		
		if(!contained)
			currentMode = enabledModes[0];
	}

	@Override
	public boolean isUsingConfig()
	{
		return isUsingConfig;
	}

	@Override
	public void setUsingConfig(boolean bool)
	{
		this.isUsingConfig = bool;
	}

	@Override
	public double getPlacementRange(GameType mode)
	{
		switch (mode)
		{
		case CREATIVE:
			return this.rangeCreative;
		default:
			return this.rangeSurvival;
		}
	}

	@Override
	public void setPlacementRange(GameType mode, double range)
	{
		switch (mode)
		{
		case CREATIVE:
			this.rangeCreative = range;
		default:
			this.rangeSurvival = range;
		}
	}

	@Override
	public boolean canPlaceInMidair(GameType mode)
	{
		switch (mode)
		{
		case CREATIVE:
			return this.midairCreative;
		default:
			return this.midairSurivival;
		}
	}

	@Override
	public void setPlaceInMidair(GameType mode, boolean bool)
	{
		switch (mode)
		{
		case CREATIVE:
			this.midairCreative = bool;
		default:
			this.midairSurivival = bool;
		}
	}

	@Override
	public int getMaxBlocksPlaced()
	{
		return this.maxBlocks;
	}

	@Override
	public void setMaxBlocksPlaced(int num)
	{
		this.maxBlocks = num;
	}

	@Override
	public double getMaxDistance()
	{
		return this.maxDistance;
	}

	@Override
	public void setMaxDistance(double num)
	{
		this.maxDistance = num;
	}

	@Override
	public boolean isBuildingActivated()
	{
		return isBuildingActive;
	}

	@Override
	public void setBuildingActivated(boolean isActive)
	{
		this.isBuildingActive = isActive;
	}

}
