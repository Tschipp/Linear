package tschipp.linear.common.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

public class Configs {
	
	public static class Settings
	{
		@Comment("Use Whitelist instead of blacklist for blocks")
		public boolean useWhitelistBlocks=false;
		
		@Comment("Place blocks in midair in survival mode")
		public boolean placeInMidairSurvival=false;
		
		@Comment("Place blocks in midair in creative mode")
		public boolean placeInMidairCreative=true;
		
		@Comment("Block placement range in survival mode")
		public double blockPlacementRangeSurvival=5;
		
		@Comment("Block placement range in creative mode")
		public double blockPlacementRangeCreative=16;
		
		@Comment("Max blocks placed in one operation (only applies to Survival mode)")
		public int maxBlocks=30;
		
		@Comment("Max distance between player and placed blocks (only applies to Survival mode)")
		public double maxDistance=20;
		
		@Comment("Enable/disable end position highlight (blue)")
		public boolean endPositionHighlight=true;
		
		@Comment("Build mode indicator xCoord")
		public float indicatorXCoord=10f;
		
		@Comment("Build mode indicator yCoord")
		public float indicatorYCoord=1045;
		
		@Comment("Multi-Place-state indicator xCoord")
		public float multiplaceXCoord=10f;
		
		@Comment("Multi-Place-state indicator yCoord")
		public float multiplaceYCoord=1035;
		
		@Comment("Build modes that are enabled by default")
		public String[] enabledBuildModes=new String[]
				{
						"line_3_axes",
						"line_2_axes",
						"line_1_axis",
						"wall_3_axes",
						"wall_2_axes"
				};
		
		@Comment("Hide Build Mode indicator")
		public boolean hideModeIndicator=false;
		
		@Comment("Hide Multi-Place-state indicator")
		public boolean hideMultiPlaceIndicator=false;
	}
	
	public static class WhiteList
	{
		@Config.RequiresMcRestart()
		@Comment("Blocks that can be multi-placed")
		public String[] allowedBlocks=new String[]
				{
				};
	}
	
	public static class Blacklist
	{
		@Config.RequiresMcRestart()
		@Comment("Blocks that cannot be multi-placed")
    	public String[] forbiddenBlocks = new String[]
    			{
    					"minecraft:chest",
    					"minecraft:trapped_chest"
    			};
		
	}

}
