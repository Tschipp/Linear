package tschipp.linear;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tschipp.linear.common.CommonProxy;

@EventBusSubscriber
@Mod(modid = Linear.MODID, name = Linear.NAME, version = Linear.VERSION, guiFactory = "tschipp.linear.client.gui.GuiFactoryLinear", dependencies = Linear.DEPENDENCIES, acceptedMinecraftVersions = Linear.ACCEPTED_VERSIONS)
public class Linear {

	@SidedProxy(clientSide = "tschipp.linear.client.ClientProxy", serverSide = "tschipp.linear.common.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(Linear.MODID)
	public static Linear instance;

	public static final String MODID = "linear";
	public static final String VERSION = "1.3";
	public static final String NAME = "Linear";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final Logger LOGGER = LogManager.getFormatterLogger("Linear");
	public static final String DEPENDENCIES = "";
	public static File CONFIGURATION_FILE;
 
	public static SimpleNetworkWrapper network;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event){
		Linear.proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		Linear.proxy.init(event);		
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Linear.proxy.postInit(event);
	}

}