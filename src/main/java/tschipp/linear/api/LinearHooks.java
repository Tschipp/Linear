package tschipp.linear.api;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import tschipp.linear.common.helper.LinearHelper;

public class LinearHooks
{

	private static final Set<Class<?>> draggableClasses = new HashSet<Class<?>>();
	
	public static void registerDraggable(Class<?> clazz)
	{
		draggableClasses.add(clazz);
	}
	
	public static Set<Class<?>> getDraggables()
	{
		return new HashSet<Class<?>>(draggableClasses);
	}
	
	public static boolean isDragging(EntityPlayer player)
	{
		return LinearHelper.hasStartPos(player);
	}
	
	public static boolean isBuildingEnabled(EntityPlayer player)
	{
		return LinearHelper.isBuildingActivated(player);
	}

}
