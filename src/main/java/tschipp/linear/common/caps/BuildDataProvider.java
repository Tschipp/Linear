package tschipp.linear.common.caps;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class BuildDataProvider implements ICapabilitySerializable
{

	@CapabilityInject(IBuildData.class)
	public static final Capability<IBuildData> BUILD_CAPABILITY = null;
	
	private IBuildData instance = BUILD_CAPABILITY.getDefaultInstance();
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == BUILD_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == BUILD_CAPABILITY ? BUILD_CAPABILITY.cast(instance) : null;
	}

	@Override
	public NBTBase serializeNBT() {
		return BUILD_CAPABILITY.getStorage().writeNBT(BUILD_CAPABILITY, instance, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		BUILD_CAPABILITY.getStorage().readNBT(BUILD_CAPABILITY, instance, null, nbt);
	}

}
