package tschipp.linear.common.caps;

import net.minecraft.util.math.BlockPos;

public interface IStartingPosition
{
	public BlockPos getStartingPosition();
	
	public void setStartingPosition(BlockPos pos);
	
	public void clear();

}
