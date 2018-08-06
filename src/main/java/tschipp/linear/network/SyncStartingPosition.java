package tschipp.linear.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.linear.common.caps.IStartingPosition;
import tschipp.linear.common.caps.StartingPositionProvider;

public class SyncStartingPosition implements IMessage, IMessageHandler<SyncStartingPosition, IMessage>
{

	public SyncStartingPosition()
	{
	}

	private IStartingPosition pos;
	private NBTTagCompound tag;

	public SyncStartingPosition(IStartingPosition pos)
	{
		this.pos = pos;
	}

	@Override
	public IMessage onMessage(SyncStartingPosition message, MessageContext ctx)
	{
		IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(new Runnable() {
			EntityPlayerMP player = ctx.getServerHandler().player;

			@Override
			public void run()
			{
				if (player.hasCapability(StartingPositionProvider.POSITION_CAPABILITY, null))
				{
					StartingPositionProvider.POSITION_CAPABILITY.getStorage().readNBT(StartingPositionProvider.POSITION_CAPABILITY, player.getCapability(StartingPositionProvider.POSITION_CAPABILITY, null), null, message.tag);
				}
			}
		});

		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		this.tag = tag;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		NBTTagCompound tag = (NBTTagCompound) StartingPositionProvider.POSITION_CAPABILITY.getStorage().writeNBT(StartingPositionProvider.POSITION_CAPABILITY, pos, null);
		ByteBufUtils.writeTag(buf, tag);
	}

}
