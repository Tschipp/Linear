package tschipp.linear.client.keybind;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LinearKeybind
{

	public static KeyBinding switchMode;
	public static KeyBinding enableBuilding;

	@SideOnly(Side.CLIENT)
	public static void init()
	{
		switchMode = new KeyBinding("key.linear.switchmode.desc", Keyboard.KEY_G, "key.linear.category");
		enableBuilding = new KeyBinding("key.linear.enablebuilding.desc", Keyboard.KEY_V, "key.linear.category");

		ClientRegistry.registerKeyBinding(switchMode);
		ClientRegistry.registerKeyBinding(enableBuilding);

	}
		
	
}
