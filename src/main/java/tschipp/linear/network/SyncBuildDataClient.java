package tschipp.linear.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import tschipp.linear.common.caps.BuildDataProvider;
import tschipp.linear.common.caps.IBuildData;

public class SyncBuildDataClient implements IMessage, IMessageHandler<SyncBuildDataClient, IMessage>
{

	public SyncBuildDataClient()
	{
	}

	private IBuildData data;
	private NBTTagCompound tag;

	public SyncBuildDataClient(IBuildData data)
	{
		this.data = data;
	}

	@Override
	public IMessage onMessage(SyncBuildDataClient message, MessageContext ctx)
	{
		IThreadListener mainThread = Minecraft.getMinecraft();

		mainThread.addScheduledTask(new Runnable() {
			EntityPlayer player = Minecraft.getMinecraft().player;

			@Override
			public void run()
			{
				if (player != null)
				{
					if (player.hasCapability(BuildDataProvider.BUILD_CAPABILITY, null))
					{
						BuildDataProvider.BUILD_CAPABILITY.getStorage().readNBT(BuildDataProvider.BUILD_CAPABILITY, player.getCapability(BuildDataProvider.BUILD_CAPABILITY, null), null, message.tag);
					}
					
				}
				else
				{
					System.out.println("player is null!");
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
