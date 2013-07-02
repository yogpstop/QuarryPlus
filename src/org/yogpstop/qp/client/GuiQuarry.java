package org.yogpstop.qp.client;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import static net.minecraft.util.StatCollector.translateToLocal;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.ContainerQuarry;
import org.yogpstop.qp.TileQuarry;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

@SideOnly(Side.CLIENT)
public class GuiQuarry extends GuiContainer {
	private TileQuarry tileQuarry;

	public GuiQuarry(EntityPlayer player, World world, int x, int y, int z) {
		super(new ContainerQuarry(player, world, x, y, z));
		this.tileQuarry = (TileQuarry) world.getBlockTileEntity(x, y, z);
		this.ySize = 238;
		this.xSize = 256;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawString(translateToLocal("tile.QuarryPlus.name"), 0);
		ArrayList<String> enchants = this.tileQuarry.getEnchantments();
		for (int i = 0; i < enchants.size(); i++) {
			drawString(enchants.get(i), i + 1);
		}
		this.fontRenderer.drawString(translateToLocal("container.inventory"), 6, 146, 0x404040);
	}

	private void drawString(String s, int count) {
		this.fontRenderer.drawString(s, (this.xSize - this.fontRenderer.getStringWidth(s)) / 2, 6 + count * 10, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture("/org/yogpstop/qp/quarry.png");
		int l = this.width - this.xSize >> 1;
		int i1 = this.height - this.ySize >> 1;
		drawTexturedModalRect(l, i1, 0, 0, this.xSize, this.ySize);
	}

	private GuiButton a3, a4, a5, a6;

	@Override
	public void initGui() {
		super.initGui();
		int i = this.width - this.xSize >> 1;
		int i2 = i + (this.xSize >> 1);
		int j = this.height - this.ySize >> 1;
		final int offset = 6;
		int half = (this.xSize - (offset << 2)) >> 1;
		int full = (this.xSize - (offset << 1));

		this.buttonList.add(new GuiButton(TileQuarry.openFortuneGui, i + offset, j + 46, half, 20, translateToLocal("gui.fortuneList")));
		this.buttonList.add(new GuiButton(TileQuarry.openSilktouchGui, i2 + offset, j + 46, half, 20, translateToLocal("gui.silktouchList")));
		this.buttonList.add(this.a3 = new GuiButton(TileQuarry.tBuildAdvFrame, i + offset, j + 71, half, 20, ""));
		this.buttonList.add(this.a4 = new GuiButton(TileQuarry.tRemoveWater, i2 + offset, j + 71, half, 20, ""));
		this.buttonList.add(this.a5 = new GuiButton(TileQuarry.tRemoveLava, i + offset, j + 96, half, 20, ""));
		this.buttonList.add(this.a6 = new GuiButton(TileQuarry.tRemoveLiquid, i2 + offset, j + 96, half, 20, ""));
		this.buttonList.add(new GuiButton(TileQuarry.reinit, i + offset, j + 121, full, 20, translateToLocal("gui.quarryReset")));
		setNames();
	}

	public void setNames() {
		this.a3.displayString = translateToLocal("gui.buildAdvFrame").concat(" : ").concat(
				translateToLocal("options.".concat(this.tileQuarry.buildAdvFrame ? "on" : "off")));
		this.a4.displayString = translateToLocal("gui.removeWater").concat(" : ").concat(
				translateToLocal("options.".concat(this.tileQuarry.removeWater ? "on" : "off")));
		this.a5.displayString = translateToLocal("gui.removeLava").concat(" : ").concat(
				translateToLocal("options.".concat(this.tileQuarry.removeLava ? "on" : "off")));
		this.a6.displayString = translateToLocal("gui.removeLiquid").concat(" : ").concat(
				translateToLocal("options.".concat(this.tileQuarry.removeLiquid ? "on" : "off")));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled) { return; }
		this.tileQuarry.sendPacketToServer((byte) par1GuiButton.id);
	}
}