package org.yogpstop.qp.client;

import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.yogpstop.qp.ContainerDummy;
import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TilePump;

public class GuiPump extends GuiContainer {
	private TilePump tilePump;

	public GuiPump(World world, int x, int y, int z) {
		super(new ContainerDummy(x, y, z));
		this.tilePump = (TilePump) world.getBlockTileEntity(x, y, z);
		this.ySize = 238;
		this.xSize = 256;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		drawString(translateToLocal("tile.QuarryPlus.name"), 0);
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

	private GuiButton b[] = new GuiButton[6];

	@Override
	public void initGui() {
		super.initGui();
		int i = this.width - this.xSize >> 1;
		int i2 = i + (this.xSize >> 1);
		int j = this.height - this.ySize >> 1;
		final int offset = 6;
		for (int k = 0; k < 6; k++)
			this.buttonList.add(this.b[k] = new GuiButton(PacketHandler.toggleLiquid_0 + k, i2 + offset, j + 24 * k, 200, 20, ""));
		setValue();
	}

	private void setValue() {
		String[] str = this.tilePump.C_getNames();
		for (int i = 0; i < 6; i++)
			this.b[i].displayString = str[i] == null ? "" : str[i];
	}

	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if (!par1GuiButton.enabled) { return; }
		PacketHandler.sendPacketToServer(this.tilePump, (byte) par1GuiButton.id);
	}
}
