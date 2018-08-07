package tschipp.linear.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import tschipp.linear.Linear;
import tschipp.linear.common.caps.BuildData;
import tschipp.linear.common.caps.BuildDataStorage;
import tschipp.linear.common.caps.IBuildData;
import tschipp.linear.common.caps.IStartingPosition;
import tschipp.linear.common.caps.StartingPosition;
import tschipp.linear.common.caps.StartingPositionStorage;
import tschipp.linear.common.event.CommonEvents;
import tschipp.linear.common.helper.ListHandler;
import tschipp.linear.network.BuildLine;
import tschipp.linear.network.SyncBuildData;
import tschipp.linear.network.SyncBuildDataClient;
import tschipp.linear.network.SyncStartingPosition;

public class CommonProxy {

	
	public void preInit(FMLPreInitializationEvent event)
	{
		
		Linear.network = NetworkRegistry.INSTANCE.newSimpleChannel("Linear");
		
		Linear.network.registerMessage(SyncStartingPosition.class, SyncStartingPosition.class, 0, Side.SERVER);
		Linear.network.registerMessage(BuildLine.class, BuildLine.class, 1, Side.SERVER);
		Linear.network.registerMessage(SyncBuildData.class, SyncBuildData.class, 2, Side.SERVER);
		Linear.network.registerMessage(SyncBuildDataClient.class, SyncBuildDataClient.class, 3, Side.CLIENT);

		MinecraftForge.EVENT_BUS.register(new CommonEvents());
	}

	
	public void init(FMLInitializationEvent event)
	{
		CapabilityManager.INSTANCE.register(IStartingPosition.class, new StartingPositionStorage(), StartingPosition::new);
		CapabilityManager.INSTANCE.register(IBuildData.class, new BuildDataStorage(), BuildData::new);
	
		ListHandler.initFilters();
	}

	
	public void postInit(FMLPostInitializationEvent e)
	{
	}
	
}
