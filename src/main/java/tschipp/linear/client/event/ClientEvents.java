package tschipp.linear.client.event;

import java.util.Set;

import net.minecraft.block.BlockColored;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import tschipp.linear.Linear;
import tschipp.linear.client.keybind.LinearKeybind;
import tschipp.linear.common.helper.FakeRenderWorld;
import tschipp.linear.common.helper.LinearHelper;
import tschipp.linear.network.BuildLine;

public class ClientEvents
{

	public static long buttonPress = 0;

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onMouse(MouseEvent event)
	{
		if (event.getButton() != 1)
			return;

		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player.isSneaking())
		{
			ItemStack stack = player.getHeldItemMainhand();
			ItemStack off = player.getHeldItemOffhand();

			if (stack.getItem() instanceof ItemBlock || off.getItem() instanceof ItemBlock)
			{
				if (event.isButtonstate())
				{
					BlockPos pos = LinearHelper.getLookPos(player, false);
					LinearHelper.setStartPos(player, pos);
					LinearHelper.syncStartPos(player);

				} else
				{
					BlockPos end = LinearHelper.getLookPos(player, LinearHelper.canPlaceInMidair(player));
					if (LinearHelper.hasStartPos(player) && LinearHelper.getBuildMode(player) != null)
					{

						Linear.network.sendToServer(new BuildLine(end));

						BlockPos start = LinearHelper.getStartPos(player);
						IBlockState state = LinearHelper.getState(player);

						Set<BlockPos> positions = LinearHelper.getBlocksBetween(player.world, state, start, end, LinearHelper.getBuildMode(player));
						positions = LinearHelper.getValidPositions(positions, player);

						for (BlockPos pos : positions)
						{
							SoundType soundtype = state.getBlock().getSoundType(state, player.world, pos, player);
							player.world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
						}

						if (positions.size() > 0)
							player.swingArm(LinearHelper.getHand(player));

						LinearHelper.clearPos(player);
						LinearHelper.syncStartPos(player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onKey(KeyInputEvent event)
	{
		if (LinearKeybind.switchMode.isPressed())
		{
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player != null)
			{
				LinearHelper.cycleBuildMode(player);
				LinearHelper.syncBuildData(player);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderGameOverlay(RenderGameOverlayEvent event)
	{
		float partialticks = event.getPartialTicks();
		ScaledResolution res = event.getResolution();
		EntityPlayer player = Minecraft.getMinecraft().player;

		GlStateManager.pushMatrix();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);

		if (LinearHelper.getHand(player) != null && LinearHelper.getBuildMode(player) != null)
			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Build Mode: " + I18n.translateToLocal("desc." + LinearHelper.getBuildMode(player).getName()), (int) (res.getScaledWidth() / 2 - (104 * res.getScaleFactor())), (int) (res.getScaledHeight() / 2 + (51 * res.getScaleFactor())), 16777215);

		if (LinearHelper.hasStartPos(player) && LinearHelper.getBuildMode(player) != null)
		{
			IBlockState state = LinearHelper.getState(player);

			if (state == null)
				return;

			BlockPos start = LinearHelper.getStartPos(player);
			BlockPos end = LinearHelper.getLookPos(player, LinearHelper.canPlaceInMidair(player));
			ItemStack stack = player.getHeldItem(LinearHelper.getHand(player));

			Set<BlockPos> positions = LinearHelper.getBlocksBetween(player.world, state, start, end, LinearHelper.getBuildMode(player));
			Set<BlockPos> valids = LinearHelper.getValidPositions(positions, player);

			// GlStateManager.pushMatrix();
			// GL11.glColor4f(1f, 1f, 1f, 1f);
			// GlStateManager.enableRescaleNormal();
			// GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, (int) (res.getScaledWidth() / 2 + (2.3 * res.getScaleFactor())), (int) (res.getScaledHeight() / 2 - (2.4 * res.getScaleFactor())));
//			RenderUtil.renderItemModelIntoGUIWithColor(stack, (int) (res.getScaledWidth() / 2 + (2.3 * res.getScaleFactor())), (int) (res.getScaledHeight() / 2 - (2.4 * res.getScaleFactor())), RenderUtil.getModelFromStack(stack, player.world), 200f, 16777215);
			
			RenderHelper.disableStandardItemLighting();
			// GlStateManager.disableRescaleNormal();
			// GlStateManager.disableBlend();
			// GlStateManager.popMatrix();

			Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("x " + valids.size(), (int) (res.getScaledWidth() / 2 + (8 * res.getScaleFactor())), (int) (res.getScaledHeight() / 2 - (1.5 * res.getScaleFactor())), positions.size() > valids.size() ? 16711680 : 16777215);

		}
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/gui/icons.png"));
		GlStateManager.color(1f, 1f, 1f);
		GlStateManager.popMatrix();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void highlightGhostBlock(RenderWorldLastEvent event)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;

		if (player.isSneaking() && LinearHelper.hasStartPos(player) && LinearHelper.getBuildMode(player) != null)
		{
			IBlockState stateToRender = LinearHelper.getState(player);
			if (stateToRender == null)
				return;

			BlockPos start = LinearHelper.getStartPos(player);
			BlockPos end = LinearHelper.getLookPos(player, LinearHelper.canPlaceInMidair(player));

			World world = player.world;

			Set<BlockPos> positions = LinearHelper.getBlocksBetween(world, stateToRender, start, end, LinearHelper.getBuildMode(player));

			Set<BlockPos> invalids = LinearHelper.getInvalidPositions(positions, player);

			FakeRenderWorld renderWorld = new FakeRenderWorld(world, positions, stateToRender);

			double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
			double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
			double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			BlockRendererDispatcher renderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();

			if (stateToRender.getBlock().getBlockLayer() != BlockRenderLayer.TRANSLUCENT)
				GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
			else
				GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

//			GlStateManager.blendFunc(world.getWorldTime() % 24000d / 12000 < 1d ? GlStateManager.SourceFactor.SRC_COLOR : GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);

			for (BlockPos toPlace : positions)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(-d0, -d1, -d2);
				GlStateManager.translate(toPlace.getX(), toPlace.getY(), toPlace.getZ());
				GlStateManager.rotate(-90, 0f, 1f, 0f);
				GlStateManager.color(1f, 1f, 1f, 0.4f);
				// GlStateManager.color(1f, 1f, 1f, 0.4f);

				IBlockState state = stateToRender;
				state = stateToRender.getActualState(renderWorld, toPlace);

				renderer.renderBlockBrightness(state, 1f);

				GlStateManager.popMatrix();
			}

			GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

			for (BlockPos toPlace : invalids)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(-d0, -d1, -d2);
				GlStateManager.translate(toPlace.getX(), toPlace.getY(), toPlace.getZ());
				GlStateManager.scale(1.01, 1.01, 1.01);
				GlStateManager.translate(-0.005, 0, 0.995);
				GL14.glBlendColor(1f, 1f, 1f, 0.5f);
				Minecraft.getMinecraft().getRenderItem().zLevel = 100f;
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

				renderer.renderBlockBrightness(Blocks.CONCRETE.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED), 1f);

				GlStateManager.popMatrix();
			}

			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			ForgeHooksClient.setRenderLayer(layer);
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

	}

}
