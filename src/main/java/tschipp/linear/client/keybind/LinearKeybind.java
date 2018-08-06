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

	public static final String KEYBIND_KEY = "switchBuildMode";
	public static KeyBinding switchMode;
	
	@SideOnly(Side.CLIENT)
	public static void init()
	{
		switchMode = new KeyBinding("key.linear.desc", Keyboard.KEY_G, "key.linear.category");
		
		ClientRegistry.registerKeyBinding(switchMode);
	}
	
	public static boolean isKeyPressed(EntityPlayer player)
	{
		NBTTagCompound tag = player.getEntityData();
		if(tag != null && tag.hasKey(KEYBIND_KEY))
		{
			return tag.getBoolean(KEYBIND_KEY);
		}
		return false;
	}
	
	public static void setKeyPressed(EntityPlayer player, boolean pressed)
	{
		NBTTagCompound tag = player.getEntityData();
		tag.setBoolean(KEYBIND_KEY, pressed);
	}
	
		
	
}
