package org.yogpstop.qp.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.ContainerQuarry;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class GuiQuarry extends GuiContainer {
	@SuppressWarnings("unused")
	private World world;
	private ContainerQuarry containerQuarry;

	public GuiQuarry(EntityPlayer player, World world, int x, int y, int z) {
		super(new ContainerQuarry(player, world, x, y, z));
		this.containerQuarry = (ContainerQuarry) inventorySlots;
		this.world = world;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(StatCollector.translateToLocal("tile.QuaryPlus.name"), 28, 6, 0x404040);
		fontRenderer.drawString(
				StatCollector.translateToLocal("container.inventory"), 28,
				(ySize - 96) + 2, 0x404040);
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

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		int i = width - xSize >> 1;
		int j = height - ySize >> 1;

		controlList.add(new GuiButton(0, i + 61, j + 33, 54, 20, "AsWrench"));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled) {
			return;
		}
		containerQuarry.onButtonPushed(par1GuiButton.id);
	}
}