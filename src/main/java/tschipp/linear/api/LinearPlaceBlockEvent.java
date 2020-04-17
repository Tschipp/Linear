package tschipp.linear.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LinearPlaceBlockEvent extends Event
{

	private final BlockPos pos;
	private final World world;
	private final EntityPlayer player;
	private final ItemStack itemStack;

	public LinearPlaceBlockEvent(BlockPos pos, EntityPlayer player, ItemStack itemStack)
	{
		this.pos = pos;
		this.player = player;
		this.itemStack = itemStack;
		this.world = player.world;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public World getWorld()
	{
		return world;
	}

	public EntityPlayer getPlayer()
	{
		return player;
	}

	public ItemStack getItemStack()
	{
		return itemStack;
	}

	
	
}
