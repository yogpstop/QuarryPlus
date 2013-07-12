package org.yogpstop.qp.client;

import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.ContainerPlayer;
import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TileMiningWell;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class GuiMiningWell extends GuiContainer {
	private TileMiningWell tileMiningWell;

	public GuiMiningWell(EntityPlayer player, World world, int x, int y, int z) {
		super(new ContainerPlayer(player, world, x, y, z));
		this.tileMiningWell = (TileMiningWell) world.getBlockTileEntity(x, y, z);
		this.ySize = 238;
		this.xSize = 256;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawString(translateToLocal("tile.MiningWellPlus.name"), 0);
		ArrayList<String> enchants = this.tileMiningWell.getEnchantments();
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

	@Override
	public void initGui() {
		super.initGui();
		int i = this.width - this.xSize >> 1;
		int i2 = i + (this.xSize >> 1);
		int j = this.height - this.ySize >> 1;
		final int offset = 6;
		int half = (this.xSize - (offset << 2)) >> 1;
		int full = (this.xSize - (offset << 1));

		this.buttonList.add(new GuiButton(PacketHandler.openFortuneGui, i + offset, j + 46, half, 20, translateToLocal("gui.fortuneList")));
		this.buttonList.add(new GuiButton(PacketHandler.openSilktouchGui, i2 + offset, j + 46, half, 20, translateToLocal("gui.silktouchList")));
		this.buttonList.add(new GuiButton(PacketHandler.reinit, i + offset, j + 121, full, 20, translateToLocal("gui.quarryReset")));
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled) { return; }
		PacketHandler.sendTilePacketToServer(this.tileMiningWell, (byte) par1GuiButton.id);
	}

}
