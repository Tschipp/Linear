package tschipp.linear.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class LinearRequestEvent extends Event
{
	private final EntityPlayer player;
	private final int requestedBlocks;
	private final ItemStack itemstack;
	private int providedBlocks;

	public EntityPlayer getPlayer()
	{
		return player;
	}

	public int getRequestedBlocks()
	{
		return requestedBlocks;
	}

	public ItemStack getItemStack()
	{
		return itemstack;
	}

	public void setProvidedBlocks(int providedBlocks)
	{
		this.providedBlocks = providedBlocks;
	}

	public int getProvidedBlocks()
	{
		return providedBlocks;
	}

	public LinearRequestEvent(EntityPlayer player, int requestedBlocks, ItemStack itemstack)
	{
		this.player = player;
		this.requestedBlocks = requestedBlocks;
		this.itemstack = itemstack;
	}

}
