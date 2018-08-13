package tschipp.linear.client.gui;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class ToastUnlockBuildmode implements IToast
{

	private String mode;
	private long firstDrawTime;

	public ToastUnlockBuildmode(String mode)
	{
		this.mode = mode;
	}

	public IToast.Visibility draw(GuiToast toastGui, long delta)
	{
		if (this.firstDrawTime == 0)
			this.firstDrawTime = delta;

		toastGui.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		toastGui.drawTexturedModalRect(0, 0, 0, 32, 160, 32);
		toastGui.getMinecraft().fontRenderer.drawString(I18n.format("toast.unlockmode.title"), 30, 7, -11534256);
		toastGui.getMinecraft().fontRenderer.drawString(I18n.format("desc." + mode), 30, 18, -16777216);
		RenderHelper.enableGUIStandardItemLighting();
		toastGui.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI((EntityLivingBase) null, new ItemStack(Blocks.STONE), 8, 8); 

		return delta - this.firstDrawTime >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;

	}

}
