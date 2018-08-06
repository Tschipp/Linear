package tschipp.linear.common.caps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StartingPositionStorage implements IStorage<IStartingPosition> {

	@Override
	public NBTBase writeNBT(Capability<IStartingPosition> capability, IStartingPosition instance, EnumFacing side) {

		NBTTagCompound tag = new NBTTagCompound();

		tag.setInteger("x", instance.getStartingPosition().getX());
		tag.setInteger("y", instance.getStartingPosition().getY());
		tag.setInteger("z", instance.getStartingPosition().getZ());
		
		return tag;

	}

	@Override
	public void readNBT(Capability<IStartingPosition> capability, IStartingPosition instance, EnumFacing side, NBTBase nbt) {

		NBTTagCompound tag = (NBTTagCompound) nbt;

		int x = tag.getInteger("x");
		int y = tag.getInteger("y");
		int z = tag.getInteger("z");
		
		BlockPos pos = new BlockPos(x,y,z);
		
		instance.setStartingPosition(pos);
	}

}
