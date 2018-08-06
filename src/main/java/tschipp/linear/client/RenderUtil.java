package tschipp.linear.client;


import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderUtil
{
	public static void renderItemWithColor(ItemStack stack, IBakedModel model, int color) {

		GlStateManager.pushMatrix();

		GlStateManager.translate(-0.5F, -0.5F, -0.5F);



		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);



		Tessellator tessellator = Tessellator.getInstance();

		BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.begin(7, DefaultVertexFormats.ITEM);



		for (EnumFacing enumfacing : EnumFacing.values()) {

			renderQuadsColor(bufferbuilder, model.getQuads((IBlockState) null, enumfacing, 0L), color, stack);

		}



		renderQuadsColor(bufferbuilder, model.getQuads((IBlockState) null, (EnumFacing) null, 0L), color, stack);

		tessellator.draw();



		GlStateManager.popMatrix();

	}



	private static void renderQuadsColor(BufferBuilder bufferbuilder, List<BakedQuad> quads, int color, ItemStack stack) {



		int i = 0;

		for (int j = quads.size(); i < j; ++i) {

			BakedQuad bakedquad = quads.get(i);



			if (bakedquad.hasTintIndex()) {

				if (EntityRenderer.anaglyphEnable) {

					color = TextureUtil.anaglyphColor(color);

				}



				color = color | -16777216;

			}



			net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(bufferbuilder, bakedquad, color);

		}

	}



	public static IBakedModel getModelFromStack(ItemStack stack, World world) {

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, world, null);

		model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.NONE, false);

		return model;

	}



	public static void renderItemModelIntoGUIWithColor(ItemStack stack, int x, int y, IBakedModel bakedmodel, float zLevel, int color) {

		GlStateManager.pushMatrix();

		Minecraft mc = Minecraft.getMinecraft();

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);

		GlStateManager.enableRescaleNormal();

		GlStateManager.enableAlpha();

		GlStateManager.alphaFunc(516, 0.1F);

		GlStateManager.enableBlend();

		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.translate(x, y, zLevel);

		GlStateManager.translate(8.0F, 8.0F, 0.0F);

		GlStateManager.scale(1.0F, -1.0F, 1.0F);

		GlStateManager.scale(16.0F, 16.0F, 16.0F);



		bakedmodel = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(bakedmodel, ItemCameraTransforms.TransformType.GUI, false);

		renderItemWithColor(stack, bakedmodel, color);



		// GlStateManager.disableAlpha();

		GlStateManager.disableBlend();

		GlStateManager.disableRescaleNormal();

		GlStateManager.disableLighting();

		GlStateManager.popMatrix();

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

	}
}
