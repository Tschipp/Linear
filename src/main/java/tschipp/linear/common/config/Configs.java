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
	}
	
	public static class WhiteList
	{
		@Config.RequiresMcRestart()
		@Comment("Blocks that CAN be picked up")
		public String[] allowedBlocks=new String[]
				{
				};
	}
	
	public static class Blacklist
	{
		@Config.RequiresMcRestart()
		@Comment("Tile Entities that cannot be picked up")
    	public String[] forbiddenBlocks = new String[]
    			{
    					"minecraft:chest",
    					"minecraft:trapped_chest"
    			};
		
	}

}
