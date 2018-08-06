package tschipp.linear.common.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import tschipp.linear.Linear;
import tschipp.linear.common.caps.BuildDataProvider;
import tschipp.linear.common.caps.IBuildData;
import tschipp.linear.common.caps.StartingPositionProvider;
import tschipp.linear.common.config.LinearConfig;
import tschipp.linear.common.helper.LinearHelper;

public class CommonEvents
{

	@SubscribeEvent
	public void onRightClick(RightClickBlock event)
	{
		EntityPlayer player = event.getEntityPlayer();
		if (player.isSneaking() && LinearHelper.getBuildMode(player) != null)
		{
			ItemStack stack = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();
			if (stack.getItem() instanceof ItemBlock || off.getItem() instanceof ItemBlock)
			{
				event.setCanceled(true);
				event.setCancellationResult(EnumActionResult.FAIL);
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		EntityLivingBase e = event.getEntityLiving();
		if (e instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) e;
			if (!player.isSneaking() && LinearHelper.hasStartPos(player))
			{
				LinearHelper.clearPos(player);
			}

			if (LinearHelper.hasStartPos(player))
			{
				IBlockState state = LinearHelper.getState(player);

				if (state == null)
					LinearHelper.clearPos(player);
			}

			if (LinearHelper.hasBuildData(player))
			{
				IBuildData data = LinearHelper.getBuildData(player);
				if (data.isUsingConfig())
				{
					if (data.canPlaceInMidair(GameType.SURVIVAL) != LinearConfig.Settings.placeInMidairSurvival)
						data.setPlaceInMidair(GameType.SURVIVAL, LinearConfig.Settings.placeInMidairSurvival);

					if (data.canPlaceInMidair(GameType.CREATIVE) != LinearConfig.Settings.placeInMidairCreative)
						data.setPlaceInMidair(GameType.CREATIVE, LinearConfig.Settings.placeInMidairCreative);

					if (data.getPlacementRange(GameType.SURVIVAL) != LinearConfig.Settings.blockPlacementRangeSurvival)
						data.setPlacementRange(GameType.SURVIVAL, LinearConfig.Settings.blockPlacementRangeSurvival);

					if (data.getPlacementRange(GameType.CREATIVE) != LinearConfig.Settings.blockPlacementRangeCreative)
						data.setPlacementRange(GameType.CREATIVE, LinearConfig.Settings.blockPlacementRangeCreative);

					if (data.getMaxBlocksPlaced() != LinearConfig.Settings.maxBlocks)
						data.setMaxBlocksPlaced(LinearConfig.Settings.maxBlocks);

					if (data.getMaxDistance() != LinearConfig.Settings.maxDistance)
						data.setMaxDistance(LinearConfig.Settings.maxDistance);
				}

			}

		}
	}

	@SubscribeEvent
	public void onAttachCaps(AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof EntityPlayer)
		{
			event.addCapability(new ResourceLocation(Linear.MODID, "starting_position"), new StartingPositionProvider());
			event.addCapability(new ResourceLocation(Linear.MODID, "build_data"), new BuildDataProvider());

		}

	}

	@SubscribeEvent
	public void onRespawn(Clone event)
	{
		EntityPlayer oldP = event.getOriginal();
		EntityPlayer newP = event.getEntityPlayer();
		
		NBTTagCompound tag = (NBTTagCompound) BuildDataProvider.BUILD_CAPABILITY.getStorage().writeNBT(BuildDataProvider.BUILD_CAPABILITY, LinearHelper.getBuildData(oldP), null);
		BuildDataProvider.BUILD_CAPABILITY.getStorage().readNBT(BuildDataProvider.BUILD_CAPABILITY, LinearHelper.getBuildData(newP), null, tag);
		if (!newP.world.isRemote)
			LinearHelper.syncBuildDataWithClient(newP);

	}
	
	@SubscribeEvent
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		if (!event.player.world.isRemote)
			LinearHelper.syncBuildDataWithClient(event.player);

	}

	@SubscribeEvent
	public void joinWorldEvent(PlayerEvent.PlayerLoggedInEvent event)
	{
		Entity e = event.player;
		if (e instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) e;
			if (!player.world.isRemote)
				LinearHelper.syncBuildDataWithClient(player);
		}

	}

}
