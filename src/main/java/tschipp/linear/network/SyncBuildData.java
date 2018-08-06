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
import tschipp.linear.common.caps.BuildDataProvider;
import tschipp.linear.common.caps.IBuildData;
import tschipp.linear.common.caps.StartingPositionProvider;

public class SyncBuildData implements IMessage, IMessageHandler<SyncBuildData, IMessage>
{

	public SyncBuildData()
	{
	}

	private IBuildData data;
	private NBTTagCompound tag;

	public SyncBuildData(IBuildData data)
	{
		this.data = data;
	}

	@Override
	public IMessage onMessage(SyncBuildData message, MessageContext ctx)
	{
		IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;

		mainThread.addScheduledTask(new Runnable() {
			EntityPlayerMP player = ctx.getServerHandler().player;

			@Override
			public void run()
			{
				if (player.hasCapability(BuildDataProvider.BUILD_CAPABILITY, null))
				{
					BuildDataProvider.BUILD_CAPABILITY.getStorage().readNBT(BuildDataProvider.BUILD_CAPABILITY, player.getCapability(BuildDataProvider.BUILD_CAPABILITY, null), null, message.tag);
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
		NBTTagCompound tag = (NBTTagCompound) BuildDataProvider.BUILD_CAPABILITY.getStorage().writeNBT(BuildDataProvider.BUILD_CAPABILITY, data, null);
		ByteBufUtils.writeTag(buf, tag);
	}

}
