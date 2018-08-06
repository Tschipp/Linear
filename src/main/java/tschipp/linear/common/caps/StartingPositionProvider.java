package tschipp.linear.common.caps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;


public class StartingPositionProvider implements ICapabilitySerializable {

	@CapabilityInject(IStartingPosition.class)
	public static final Capability<IStartingPosition> POSITION_CAPABILITY = null;
	
	private IStartingPosition instance = POSITION_CAPABILITY.getDefaultInstance();
	
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == POSITION_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == POSITION_CAPABILITY ? POSITION_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return POSITION_CAPABILITY.getStorage().writeNBT(POSITION_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		POSITION_CAPABILITY.getStorage().readNBT(POSITION_CAPABILITY, instance, null, nbt);
	}

}
