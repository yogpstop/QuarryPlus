package org.yogpstop.qp.client;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.StatCollector;
import net.minecraft.src.World;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.ContainerQuarry;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;


@SideOnly(Side.CLIENT)
public class GuiQuarry extends GuiContainer {
	private World world;
	private ContainerQuarry containerQuarry;

	public GuiQuarry(EntityPlayer player, World world, int x, int y, int z) {
		super(new ContainerQuarry(player, world, x, y, z));
		this.containerQuarry = (ContainerQuarry) inventorySlots;
		this.world = world;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString("QuarryPlus", 60, 6, 0x404040);
		fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 26, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		int k = mc.renderEngine.getTexture("/org/yogpstop/qp/quarry.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(k);
		int l = width - xSize >> 1;
		int i1 = height - ySize >> 1;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}
}