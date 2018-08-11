package tschipp.linear.common.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tschipp.linear.Linear;
import tschipp.linear.common.caps.BuildDataProvider;
import tschipp.linear.common.caps.IBuildData;
import tschipp.linear.common.caps.IStartingPosition;
import tschipp.linear.common.caps.StartingPositionProvider;
import tschipp.linear.common.config.LinearConfig;
import tschipp.linear.network.SyncBuildData;
import tschipp.linear.network.SyncBuildDataClient;
import tschipp.linear.network.SyncStartingPosition;

public class LinearHelper
{

	public static boolean hasStartPos(EntityPlayer player)
	{
		if (player.hasCapability(StartingPositionProvider.POSITION_CAPABILITY, null))
		{
			IStartingPosition pos = player.getCapability(StartingPositionProvider.POSITION_CAPABILITY, null);
			if (pos.getStartingPosition().equals(new BlockPos(-1, -1, -1)))
				return false;
			else
				return true;
		}
		return false;
	}

	public static BlockPos getStartPos(EntityPlayer player)
	{
		if (player.hasCapability(StartingPositionProvider.POSITION_CAPABILITY, null))
		{
			IStartingPosition pos = player.getCapability(StartingPositionProvider.POSITION_CAPABILITY, null);

			return pos.getStartingPosition();
		}
		return null;
	}

	public static void setStartPos(EntityPlayer player, BlockPos pos)
	{
		if (player.hasCapability(StartingPositionProvider.POSITION_CAPABILITY, null))
		{
			IStartingPosition cap = player.getCapability(StartingPositionProvider.POSITION_CAPABILITY, null);

			cap.setStartingPosition(pos);
		}
	}

	public static void clearPos(EntityPlayer player)
	{
		if (player.hasCapability(StartingPositionProvider.POSITION_CAPABILITY, null))
		{
			IStartingPosition cap = player.getCapability(StartingPositionProvider.POSITION_CAPABILITY, null);

			cap.clear();
		}
	}

	public static boolean hasBuildData(EntityPlayer player)
	{
		return player.hasCapability(BuildDataProvider.BUILD_CAPABILITY, null);
	}

	public static IBuildData getBuildData(EntityPlayer player)
	{
		if (player.hasCapability(BuildDataProvider.BUILD_CAPABILITY, null))
		{
			IBuildData data = player.getCapability(BuildDataProvider.BUILD_CAPABILITY, null);

			return data;
		}
		return null;
	}

	public static boolean isBuildingActivated(EntityPlayer player)
	{
		IBuildData data = getBuildData(player);
		if (data != null)
		{
			return data.isBuildingActivated();
		}
		return false;
	}

	public static ArrayList<BlockPos> getBlocksBetween(World world, IBlockState state, BlockPos a, BlockPos b, BuildMode mode, EntityPlayer player)
	{
		if (a.equals(new BlockPos(-1, -1, -1)) || b.equals(new BlockPos(-1, -1, -1)))
			return new ArrayList<BlockPos>();

		if (state.getBlock() == Blocks.AIR)
			return new ArrayList<BlockPos>();

		Set<BlockPos> set = new HashSet<BlockPos>();
		int axis = mode.getAxis();

		Block block = state.getBlock();

		EnumFacing facing = LinearHelper.getFacing(player);

		boolean yFlag = false;
		BlockPos c = null;
		if (mode.isPlane())
		{
			if (a.getY() == b.getY())
			{
				if (a.getX() > b.getX())
				{
					BlockPos temp = a;
					a = b;
					b = temp;
				}

				c = b;
				b = new BlockPos(a.getX(), b.getY(), b.getZ());
			}
			else
			{
				if (a.getY() > b.getY())
				{
					BlockPos temp = new BlockPos(a);
					a = new BlockPos(b);
					b = new BlockPos(temp);
					yFlag = true;
				}

				c = b;
				b = new BlockPos(b.getX(), a.getY(), b.getZ());
			}
		}

		double distance = a.getDistance(b.getX(), b.getY(), b.getZ());

		double incrementX = distance == 0 ? 0 : (-a.getX() + b.getX()) / distance;
		double incrementY = distance == 0 ? 0 : (-a.getY() + b.getY()) / distance;
		double incrementZ = distance == 0 ? 0 : (-a.getZ() + b.getZ()) / distance;

		double differenceX = Math.abs(Math.abs(a.getX()) - Math.abs(b.getX()));
		double differenceY = Math.abs(Math.abs(a.getY()) - Math.abs(b.getY()));
		double differenceZ = Math.abs(Math.abs(a.getZ()) - Math.abs(b.getZ()));

		FakeRenderWorld fakeWorld = new FakeRenderWorld(world, new ArrayList<BlockPos>(set), state);

		for (int i = 0; i <= distance; i++)
		{

			BlockPos toPlace;

			double x = Math.floor((double) a.getX() + (incrementX * i));
			double y = Math.floor((double) a.getY() + (incrementY * i));
			double z = Math.floor((double) a.getZ() + (incrementZ * i));

			if (axis == 2 && mode.isPlane() && yFlag)
			{
				x = Math.floor((double) b.getX() + (-incrementX * i));
				y = Math.floor((double) b.getY() + (-incrementY * i));
				z = Math.floor((double) b.getZ() + (-incrementZ * i));
			}

			if (axis == 2 && !mode.isPlane())
				toPlace = new BlockPos((differenceX > differenceY || differenceX > differenceZ) ? x : a.getX(), (differenceY > differenceX || differenceY > differenceZ) ? y : a.getY(), (differenceZ > differenceY || differenceZ > differenceX) ? z : a.getZ());
			else if (axis == 1 || (axis == 2 && mode.isPlane() && !yFlag))
				toPlace = new BlockPos((differenceX > differenceY && differenceX > differenceZ) ? x : a.getX(), (differenceY > differenceX && differenceY > differenceZ) ? y : a.getY(), (differenceZ > differenceX && differenceZ > differenceY) ? z : a.getZ());
			else if (axis == 2 && mode.isPlane() && yFlag)
				toPlace = new BlockPos((differenceX > differenceY && differenceX > differenceZ) ? x : b.getX(), (differenceY > differenceX && differenceY > differenceZ) ? y : b.getY(), (differenceZ > differenceX && differenceZ > differenceY) ? z : b.getZ());
			else
				toPlace = new BlockPos(x, y, z);

			if ((world.mayPlace(block, toPlace, false, facing, null)) || mode.isPlane())
				set.add(toPlace);

		}

		if (world.mayPlace(block, a, false, facing, null) && axis == 3)
			set.add(a);

		if (world.mayPlace(block, b, false, facing, null) && axis == 3)
			set.add(b);

		if (mode.isPlane())
		{
			differenceY = Math.abs(Math.abs(a.getY()) - Math.abs(c.getY()));
			differenceX = Math.abs(Math.abs(a.getX()) - Math.abs(c.getX()));

			Set<BlockPos> morePos = new HashSet<BlockPos>();

			for (BlockPos pos : set)
			{

				if (differenceY > 0)
				{
					for (int i = 0; i <= differenceY; i++)
					{
						morePos.add(new BlockPos(pos.getX(), pos.getY() + i, pos.getZ()));
					}
				}
				else if (differenceX > 0)
				{
					for (int i = 0; i <= differenceX; i++)
					{
						morePos.add(new BlockPos(pos.getX() + i, pos.getY(), pos.getZ()));
					}
				}

			}

			set.addAll(morePos);

		}

		set.removeIf(pos -> !world.mayPlace(block, pos, false, facing, null));

		ArrayList<BlockPos> l = new ArrayList<BlockPos>(set);

		final BlockPos playerPos = new BlockPos(Math.floor(player.posX) + 0.5, Math.floor(player.posY), Math.floor(player.posZ) + 0.5);

		l.sort((pos1, pos2) -> (pos1.distanceSq(playerPos) > pos2.distanceSq(playerPos) ? 1 : (pos1.distanceSq(playerPos) == pos2.distanceSq(playerPos) ? 0 : -1)));

		return l;
	}

	public static ArrayList<BlockPos> getValidPositions(ArrayList<BlockPos> posSet, EntityPlayer player)
	{
		ItemStack stack = player.getHeldItem(LinearHelper.getHand(player));
		List<ItemStack> inventory = new ArrayList<ItemStack>();
		inventory.addAll(player.inventory.mainInventory);
		inventory.addAll(player.inventory.armorInventory);
		inventory.addAll(player.inventory.offHandInventory);

		final BlockPos playerPos = new BlockPos(Math.floor(player.posX) + 0.5, Math.floor(player.posY), Math.floor(player.posZ) + 0.5);

		int count = 0;

		for (ItemStack s : inventory)
		{
			if (s.isItemEqual(stack))
				count += s.getCount();
		}

		List<BlockPos> positions = new ArrayList<BlockPos>(posSet);
		positions.sort((pos1, pos2) -> (pos1.distanceSq(playerPos) > pos2.distanceSq(playerPos) ? 1 : (pos1.distanceSq(playerPos) == pos2.distanceSq(playerPos) ? 0 : -1)));

		if (!player.isCreative())
		{
			int positionsToRemove = positions.size() - count;
			if (positionsToRemove > 0)
			{
				for (int i = 0; i < positionsToRemove; i++)
					positions.remove(positions.size() - 1);
			}

			positions.removeIf(pos -> Math.sqrt(pos.distanceSq(playerPos)) > LinearHelper.getMaxPlacementDistance(player));

			if (positions.size() > LinearHelper.getMaxBlocksPlaced(player))
				positions = positions.subList(0, LinearHelper.getMaxBlocksPlaced(player));
		}

		return new ArrayList<BlockPos>(positions);
	}

	public static void removeItems(ItemStack stack, EntityPlayer player, int count)
	{
		player.inventory.clearMatchingItems(stack.getItem(), stack.getMetadata(), count, null);
	}

	public static ArrayList<BlockPos> getInvalidPositions(ArrayList<BlockPos> posSet, EntityPlayer player)
	{
		ArrayList<BlockPos> valid = getValidPositions(posSet, player);
		ArrayList<BlockPos> newSet = new ArrayList<BlockPos>(posSet);
		newSet.removeAll(valid);

		return newSet;
	}

	public static BlockPos getLookPos(EntityPlayer player, boolean midair)
	{
		RayTraceResult ray = getLookRay(player);
		if (ray == null)
			return new BlockPos(-1, -1, -1);

		BlockPos pos = ray.getBlockPos();
		if (player.world.getBlockState(pos).getMaterial() == Material.AIR && !midair)
			return new BlockPos(-1, -1, -1);

		if (!player.world.getBlockState(pos).getBlock().isReplaceable(player.world, pos))
			pos = pos.offset(ray.sideHit);

		return pos;
	}

	public static RayTraceResult getLookRay(EntityPlayer player)
	{

		World world = player.world;
		Vec3d look = player.getLookVec();
		Vec3d start = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		Vec3d end = new Vec3d(player.posX + look.x * LinearHelper.getPlacementRange(player), player.posY + player.getEyeHeight() + look.y * LinearHelper.getPlacementRange(player), player.posZ + look.z * LinearHelper.getPlacementRange(player));
		return world.rayTraceBlocks(start, end, false, false, true);
	}

	public static EnumFacing getFacing(EntityPlayer player)
	{
		RayTraceResult ray = getLookRay(player);
		if (ray != null)
		{
			return ray.sideHit;
		}
		return EnumFacing.DOWN;
	}

	public static IBlockState getState(EntityPlayer player)
	{
		if (hasValidItem(player))
		{
			RayTraceResult ray = getLookRay(player);
			if (ray != null)
			{
				ItemStack stack = getValidItem(player);

				Block block = Block.getBlockFromItem(stack.getItem());

				IBlockState state = block.getStateForPlacement(player.world, getLookPos(player, LinearHelper.canPlaceInMidair(player)), ray.sideHit, 0, 0, 0, stack.getMetadata(), player, getHand(player));
				return state;
			}
		}
		return null;
	}

	public static EnumHand getHand(EntityPlayer player)
	{
		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();

		ItemStack stack;
		EnumHand hand;

		if (isValid(main))
			return EnumHand.MAIN_HAND;
		else if (isValid(off))
			return EnumHand.OFF_HAND;
		else
			return null;
	}

	public static boolean hasValidItem(EntityPlayer player)
	{
		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();

		if (isValid(main))
			return true;

		if (isValid(off))
			return true;

		return false;
	}

	private static boolean isValid(ItemStack stack)
	{
		if (stack.getItem() instanceof ItemBlock)
		{
			if (!LinearConfig.Settings.useWhitelistBlocks)
			{
				if (!ListHandler.isForbidden(Block.getBlockFromItem(stack.getItem())))
					return true;
			}
			else
			{
				if (ListHandler.isAllowed(Block.getBlockFromItem(stack.getItem())))
					return true;
			}
		}

		return false;
	}

	public static ItemStack getValidItem(EntityPlayer player)
	{
		if (hasValidItem(player))
		{
			return player.getHeldItem(getHand(player));
		}
		return ItemStack.EMPTY;
	}

	@SideOnly(Side.CLIENT)
	public static void syncStartPos(EntityPlayer player)
	{
		if (player.hasCapability(StartingPositionProvider.POSITION_CAPABILITY, null))
			Linear.network.sendToServer(new SyncStartingPosition(player.getCapability(StartingPositionProvider.POSITION_CAPABILITY, null)));
	}

	@SideOnly(Side.CLIENT)
	public static void syncBuildData(EntityPlayer player)
	{
		if (player.hasCapability(BuildDataProvider.BUILD_CAPABILITY, null))
			Linear.network.sendToServer(new SyncBuildData(player.getCapability(BuildDataProvider.BUILD_CAPABILITY, null)));
	}

	public static void syncBuildDataWithClient(EntityPlayer player)
	{
		if (player.hasCapability(BuildDataProvider.BUILD_CAPABILITY, null))
			Linear.network.sendTo(new SyncBuildDataClient(player.getCapability(BuildDataProvider.BUILD_CAPABILITY, null)), (EntityPlayerMP) player);
	}

	public static double getPlacementRange(EntityPlayer player)
	{
		if (hasBuildData(player))
		{
			IBuildData data = getBuildData(player);
			if (player.isCreative())
				return data.getPlacementRange(GameType.CREATIVE);
			else
				return data.getPlacementRange(GameType.SURVIVAL);
		}
		return 0;

	}

	public static boolean canPlaceInMidair(EntityPlayer player)
	{
		if (hasBuildData(player))
		{
			IBuildData data = getBuildData(player);
			if (player.isCreative())
				return data.canPlaceInMidair(GameType.CREATIVE);
			else
				return data.canPlaceInMidair(GameType.SURVIVAL);
		}
		return false;

	}

	public static int getMaxBlocksPlaced(EntityPlayer player)
	{
		if (hasBuildData(player))
		{
			IBuildData data = getBuildData(player);
			return data.getMaxBlocksPlaced();
		}
		return 0;
	}

	public static double getMaxPlacementDistance(EntityPlayer player)
	{
		if (hasBuildData(player))
		{
			IBuildData data = getBuildData(player);
			return data.getMaxDistance();
		}
		return 0;

	}

	@Nullable
	public static BuildMode getBuildMode(EntityPlayer player)
	{
		if (hasBuildData(player))
		{
			IBuildData data = getBuildData(player);
			return data.getCurrentBuildMode();
		}
		return null;
	}

	public static void cycleBuildMode(EntityPlayer player)
	{
		if (hasBuildData(player))
		{
			IBuildData data = getBuildData(player);
			BuildMode[] modes = data.getEnabledBuildModes();
			BuildMode current = data.getCurrentBuildMode();

			if (current != null && modes.length > 1)
			{
				int idx = -1;
				for (int i = 0; i < modes.length; i++)
				{
					BuildMode mode = modes[i];
					if (mode == current)
						idx = i;
				}

				if (idx > -1)
				{
					if (idx == modes.length - 1)
						data.setCurrentBuildMode(modes[0]);
					else
						data.setCurrentBuildMode(modes[idx + 1]);
				}
			}
		}
	}

	public static BuildMode[] getBuildModesFromConfig()
	{
		ArrayList<BuildMode> list = new ArrayList<BuildMode>();

		for (String s : LinearConfig.Settings.enabledBuildModes)
		{
			BuildMode m = BuildMode.getByName(s);
			if (m != null)
				list.add(m);
		}

		BuildMode[] modes = list.toArray(new BuildMode[list.size()]);
		return modes;
	}
}
