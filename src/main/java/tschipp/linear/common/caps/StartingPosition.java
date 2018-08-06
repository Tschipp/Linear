package tschipp.linear.common.caps;

import net.minecraft.util.math.BlockPos;

public class StartingPosition implements IStartingPosition
{
	
	BlockPos pos = new BlockPos(-1,-1,-1);
	
	@Override
	public BlockPos getStartingPosition()
	{
		return pos;
	}

	@Override
	public void setStartingPosition(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void clear()
	{
		pos = new BlockPos(-1,-1,-1);
	}

}
