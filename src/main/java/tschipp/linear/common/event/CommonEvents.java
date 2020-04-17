package tschipp.linear.common.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import tschipp.linear.Linear;
import tschipp.linear.api.LinearBlockStateEvent;
import tschipp.linear.api.LinearPlaceBlockEvent;
import tschipp.linear.api.LinearRequestEvent;
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

		if (player.isSneaking() && LinearHelper.getBuildMode(player) != null && LinearHelper.hasValidItem(player) && LinearHelper.isBuildingActivated(player))
		{

			event.setCanceled(true);
			event.setCancellationResult(EnumActionResult.FAIL);
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

			if (!player.world.isRemote)
			{
				if (LinearHelper.hasBuildData(player))
				{
					IBuildData data = LinearHelper.getBuildData(player);
					if (data.isUsingConfig())
					{
						boolean didChange = false;

						if (data.canPlaceInMidair(GameType.SURVIVAL) != LinearConfig.Settings.placeInMidairSurvival)
						{
							data.setPlaceInMidair(GameType.SURVIVAL, LinearConfig.Settings.placeInMidairSurvival);
							didChange = true;
						}

						if (data.canPlaceInMidair(GameType.CREATIVE) != LinearConfig.Settings.placeInMidairCreative)
						{
							data.setPlaceInMidair(GameType.CREATIVE, LinearConfig.Settings.placeInMidairCreative);
							didChange = true;
						}

						if (data.getPlacementRange(GameType.SURVIVAL) != LinearConfig.Settings.blockPlacementRangeSurvival)
						{
							data.setPlacementRange(GameType.SURVIVAL, LinearConfig.Settings.blockPlacementRangeSurvival);
							didChange = true;
						}

						if (data.getPlacementRange(GameType.CREATIVE) != LinearConfig.Settings.blockPlacementRangeCreative)
						{
							data.setPlacementRange(GameType.CREATIVE, LinearConfig.Settings.blockPlacementRangeCreative);
							didChange = true;
						}

						if (data.getMaxBlocksPlaced() != LinearConfig.Settings.maxBlocks)
						{
							data.setMaxBlocksPlaced(LinearConfig.Settings.maxBlocks);
							didChange = true;
						}

						if (data.getMaxDistance() != LinearConfig.Settings.maxDistance)
						{
							data.setMaxDistance(LinearConfig.Settings.maxDistance);
							didChange = true;
						}

						if (!Arrays.equals(data.getEnabledBuildModes(), LinearHelper.getBuildModesFromConfig()))
						{
							data.setEnabledBuildModes(LinearHelper.getBuildModesFromConfig());
							didChange = true;
						}

						if (didChange)
							LinearHelper.syncBuildDataWithClient(player);
					}

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

	@SubscribeEvent
	public void linearRequestEvent(LinearRequestEvent event)
	{
		EntityPlayer player = event.getPlayer();
		ItemStack stack = event.getItemStack();
		int blockAmount = event.getRequestedBlocks();

		List<ItemStack> inventory = new ArrayList<ItemStack>();
		inventory.addAll(player.inventory.mainInventory);
		inventory.addAll(player.inventory.armorInventory);
		inventory.addAll(player.inventory.offHandInventory);

		int count = 0;

		if (stack.getItem() instanceof ItemBlock)
		{
			for (ItemStack s : inventory)
			{
				if (s.isItemEqual(stack))
					count += s.getCount();
			}
		}

		event.setProvidedBlocks(event.getProvidedBlocks() + count);
	}

	@SubscribeEvent
	public void linearPlaceBlockEvent(LinearPlaceBlockEvent event)
	{
		EntityPlayer player = event.getPlayer();
		ItemStack stack = event.getItemStack();
		World world = event.getWorld();
		BlockPos pos = event.getPos();

		if (stack.getItem() instanceof ItemBlock)
		{
			if (!world.isRemote)
			{
				IBlockState state = LinearHelper.getState(player);
				float[] hit = LinearHelper.getHitCoords(player);
				EnumHand hand = LinearHelper.getHand(player);

				FakePlayer fake = new FakePlayer((WorldServer) world, player.getGameProfile());

				ItemStack copy = stack.copy();

				fake.setHeldItem(hand, copy);
				fake.setPosition(player.posX, player.posY, player.posZ);
				fake.rotationYaw = player.rotationYaw;
				player.rotationPitch = player.rotationPitch;

				((ItemBlock) copy.getItem()).onItemUse(fake, world, pos, hand, LinearHelper.getFacing(player), hit[0], hit[1], hit[2]);
				SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

				if (!player.isCreative())
					LinearHelper.removeItems(stack, player, 1);
			}
		}

	}

	@SubscribeEvent
	public void linearBlockStateEvent(LinearBlockStateEvent event)
	{
		EntityPlayer player = event.getPlayer();
		ItemStack stack = event.getStack();
		EnumHand hand = event.getHand();

		if (stack.getItem() instanceof ItemBlock)
		{
			RayTraceResult ray = LinearHelper.getLookRay(player);
			if (ray != null)
			{
				Block block = Block.getBlockFromItem(stack.getItem());

				float[] hit = LinearHelper.getHitCoords(player);
				IBlockState state = block.getStateForPlacement(player.world, LinearHelper.getLookPos(player, LinearHelper.canPlaceInMidair(player)), ray.sideHit, hit[0], hit[1], hit[2], stack.getMetadata(), player, hand);

				if (state != null)
					event.setState(state);

			}
		}
	}

}
