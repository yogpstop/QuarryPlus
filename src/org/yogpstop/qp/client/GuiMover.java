package org.yogpstop.qp.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import org.yogpstop.qp.ContainerMover;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class GuiMover extends GuiContainer {
	public GuiButton a1, a3, a5;

	public GuiMover(EntityPlayer player, World world, int x, int y, int z) {
		super(null);
		this.inventorySlots = new ContainerMover(player, world, x, y, z, this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int i = width - xSize >> 1;
		int j = height - ySize >> 1;
		a1 = new GuiButton(1, i + 126, j + 20, 20, 15, ">>");
		a3 = new GuiButton(3, i + 126, j + 36, 20, 15, ">>");
		a5 = new GuiButton(5, i + 126, j + 52, 20, 15, ">>");
		controlList.add(a1);
		controlList.add(a3);
		controlList.add(a5);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawString(StatCollector.translateToLocal("tile.EnchantMover.name"), 0);
		drawString(StatCollector.translateToLocal("enchantment.untouching"), 1);
		drawString(StatCollector.translateToLocal("enchantment.lootBonus"), 2);
		drawString(StatCollector.translateToLocal("enchantment.digging"), 3);
		drawString(StatCollector.translateToLocal("container.inventory"), 4);
	}

	private void drawString(String s, int i) {
		fontRenderer.drawString(s,
				(xSize - fontRenderer.getStringWidth(s)) / 2,
				(int) (6F + 16.5F * (float) i), 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		int k = mc.renderEngine.getTexture("/org/yogpstop/qp/mover.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(k);
		int l = width - xSize >> 1;
		int i1 = height - ySize >> 1;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled)
			return;
		((ContainerMover) inventorySlots).onButtonPushed(par1GuiButton.id);
	}
}