package tschipp.linear.network;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.linear.common.helper.BuildMode;
import tschipp.linear.common.helper.LinearHelper;

public class BuildLine implements IMessage, IMessageHandler<BuildLine, IMessage>
{

	private BlockPos pos;

	public BuildLine()
	{
	}

	public BuildLine(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public IMessage onMessage(BuildLine message, MessageContext ctx)
	{
		IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(new Runnable() {
			EntityPlayerMP player = ctx.getServerHandler().player;

			@Override
			public void run()
			{
				World world = player.world;
				BlockPos start = LinearHelper.getStartPos(player);
				IBlockState state = LinearHelper.getState(player);
				ItemStack stack = player.getHeldItem(LinearHelper.getHand(player));

				Set<BlockPos> positions = LinearHelper.getBlocksBetween(world, state, start, message.pos, LinearHelper.getBuildMode(player));
				positions = LinearHelper.getValidPositions(positions, player);

				for (BlockPos pos : positions)
				{
					world.setBlockState(pos, state);
					SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				}

				if (positions.size() > 0)
					player.swingArm(LinearHelper.getHand(player));

				if (!player.isCreative())
					LinearHelper.removeItems(stack, player, positions.size());
			}
		});

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		BlockPos pos = NBTUtil.getPosFromTag(tag);
		this.pos = pos;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		NBTTagCompound tag = NBTUtil.createPosTag(pos);
		ByteBufUtils.writeTag(buf, tag);
	}

}
