package tschipp.linear.common.helper;

import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class FakeRenderWorld implements IBlockAccess
{

	private World realWorld;
	private Set<BlockPos> positions;
	private IBlockState state;
	
	public FakeRenderWorld(World world, Set<BlockPos> positions, IBlockState state)
	{
		this.realWorld = world;
		this.positions = positions;
		this.state = state;
	}
	
	@Override
	public TileEntity getTileEntity(BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue)
	{
		return realWorld.getCombinedLight(pos, lightValue);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos)
	{
		if(positions.contains(pos))
			return state;
		return realWorld.getBlockState(pos);
	}

	@Override
	public boolean isAirBlock(BlockPos pos)
	{
		 if(!positions.contains(pos) && realWorld.isAirBlock(pos))
			 return true;
		 else if(positions.contains(pos))
			 return false;
		 else return realWorld.isAirBlock(pos);
	}

	@Override
	public Biome getBiome(BlockPos pos)
	{
		return realWorld.getBiome(pos);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction)
	{
		return realWorld.getStrongPower(pos, direction);
	}

	@Override
	public WorldType getWorldType()
	{
		return realWorld.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
	{
		return getBlockState(pos).isSideSolid(this, pos, side);
	}

}
