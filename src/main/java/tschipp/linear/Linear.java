package tschipp.linear;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import tschipp.linear.common.CommonProxy;

@EventBusSubscriber
@Mod(modid = Linear.MODID, name = Linear.NAME, version = Linear.VERSION, guiFactory = "tschipp.linear.client.gui.GuiFactoryLinear", dependencies = Linear.DEPENDENCIES, acceptedMinecraftVersions = Linear.ACCEPTED_VERSIONS, certificateFingerprint =  Linear.CERTIFICATE_FINGERPRINT)
public class Linear {

	@SidedProxy(clientSide = "tschipp.linear.client.ClientProxy", serverSide = "tschipp.linear.common.CommonProxy")
	public static CommonProxy proxy;

	// Instance
	@Instance(Linear.MODID)
	public static Linear instance;

	public static final String MODID = "linear";
	public static final String VERSION = "GRADLE:VERSION";
	public static final String NAME = "Linear";
	public static final String ACCEPTED_VERSIONS = "[1.12.2,1.13)";
	public static final Logger LOGGER = LogManager.getFormatterLogger("Linear");
	public static final String DEPENDENCIES = "";
	public static File CONFIGURATION_FILE;
	public static final String CERTIFICATE_FINGERPRINT = "fd21553434f4905f2f73ea7838147ac4ea07bd88";

	public static boolean FINGERPRINT_VIOLATED = false;
	
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

	@EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        
		LOGGER.error("WARNING! Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with! If you didn't download the file from https://minecraft.curseforge.com/projects/linear or through any kind of mod launcher, immediately delete the file and re-download it from https://minecraft.curseforge.com/projects/linear");
		FINGERPRINT_VIOLATED = true;
	}
	
}