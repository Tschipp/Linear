package tschipp.linear.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import tschipp.linear.client.event.ClientEvents;
import tschipp.linear.client.keybind.LinearKeybind;
import tschipp.linear.common.CommonProxy;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		super.preInit(event);
//		CarryOn.network = NetworkRegistry.INSTANCE.newSimpleChannel("CarryOn");
//		
//		CarryOn.network.registerMessage(SyncKeybindPacketHandler.class, SyncKeybindPacket.class, 0, Side.SERVER);
//		CarryOn.network.registerMessage(CarrySlotPacketHandler.class, CarrySlotPacket.class, 1, Side.CLIENT);
//		CarryOn.network.registerMessage(ScriptReloadPacketHandler.class, ScriptReloadPacket.class, 2, Side.CLIENT);

		MinecraftForge.EVENT_BUS.register(new ClientEvents());
	}

	@Override
	public void init(FMLInitializationEvent event)
	{
		super.init(event);
		LinearKeybind.init();
	}

	@Override
	public void postInit(FMLPostInitializationEvent e)
	{
		super.postInit(e);
	}
	
}
