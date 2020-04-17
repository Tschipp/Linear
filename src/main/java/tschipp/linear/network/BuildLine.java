package tschipp.linear.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.linear.api.LinearPlaceBlockEvent;
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

		mainThread.addScheduledTask(new Runnable()
		{
			EntityPlayerMP player = ctx.getServerHandler().player;

			@Override
			public void run()
			{
				World world = player.world;
				BlockPos start = LinearHelper.getStartPos(player);
				ItemStack stack = LinearHelper.getValidItem(player);
				IBlockState state = LinearHelper.getState(player);

				EnumHand hand = LinearHelper.getHand(player);
				
				int originalCount = stack.getCount();

				List<BlockPos> positions = LinearHelper.getBlocksBetween(world, state, start, message.pos, LinearHelper.getBuildMode(player), player);
				positions = LinearHelper.getValidPositions(positions, player);

				int blocksPlaced = 0;
				float[] hit = LinearHelper.getHitCoords(player);

				for (BlockPos pos : positions)
				{
					BlockSnapshot snapshot = BlockSnapshot.getBlockSnapshot(world, pos);
					PlaceEvent event = ForgeEventFactory.onPlayerBlockPlace(player, snapshot, EnumFacing.DOWN, LinearHelper.getHand(player));

					if (!event.isCanceled())
					{
						LinearPlaceBlockEvent placeBlock = new LinearPlaceBlockEvent(pos, player, stack);
						MinecraftForge.EVENT_BUS.post(placeBlock);
						
						if(placeBlock.isCanceled())
							continue;
						
						blocksPlaced++;
					}
				}

				if (positions.size() > 0 && blocksPlaced > 0)
					player.swingArm(hand);
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
