package tschipp.linear.api;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.Event;

public class LinearBlockStateEvent extends Event
{
	private final EntityPlayer player;
	private final ItemStack stack;
	private final EnumHand hand;

	private IBlockState state;

	public EntityPlayer getPlayer()
	{
		return player;
	}

	public ItemStack getStack()
	{
		return stack;
	}

	public EnumHand getHand()
	{
		return hand;
	}

	public IBlockState getState()
	{
		return state;
	}

	public void setState(@Nonnull IBlockState state)
	{
		this.state = state;
	}

	public LinearBlockStateEvent(EntityPlayer player, ItemStack stack, EnumHand hand)
	{
		this.player = player;
		this.stack = stack;
		this.hand = hand;
	}

}
