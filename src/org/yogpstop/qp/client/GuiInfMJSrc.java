package org.yogpstop.qp.client;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TileInfMJSrc;

@SideOnly(Side.CLIENT)
public class GuiInfMJSrc extends GuiScreen {

	private GuiTextField eng;
	private GuiTextField itv;
	private boolean act = false;
	private float pw;
	private int it;
	private static final ResourceLocation gui = new ResourceLocation("yogpstop_qp", "textures/gui/infmjsrc.png");
	public int x, y, z;

	public GuiInfMJSrc(int ax, int ay, int az, World aw) {
		this.x = ax;
		this.y = ay;
		this.z = az;
		TileEntity te = aw.getBlockTileEntity(ax, ay, az);
		if (te instanceof TileInfMJSrc) {
			this.act = ((TileInfMJSrc) te).active;
			this.pw = ((TileInfMJSrc) te).power;
			this.it = ((TileInfMJSrc) te).interval;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// int xb = (this.width - 176) >> 1;
		int yb = (this.height - 214) >> 1;
		this.eng = new GuiTextField(this.fontRenderer, (this.width >> 1) - 75, yb + 58, 150, 20);
		this.eng.setText(Float.toString(this.pw));
		this.itv = new GuiTextField(this.fontRenderer, (this.width >> 1) - 75, yb + 106, 150, 20);
		this.itv.setText(Integer.toString(this.it));
		this.buttonList.add(new GuiButton(1, (this.width >> 1) + 30, yb + 34, 50, 20, "Reset"));
		this.buttonList.add(new GuiButton(2, (this.width >> 1) + 30, yb + 82, 50, 20, "Reset"));
		this.buttonList.add(new GuiButton(3, (this.width >> 1) - 75, yb + 144, 150, 20, "Apply"));
		this.buttonList.add(new GuiButton(4, (this.width >> 1) - 75, yb + 168, 150, 20, this.act ? "Active" : "Deactive"));
	}

	@Override
	protected void actionPerformed(GuiButton gb) {
		if (!gb.enabled) { return; }
		switch (gb.id) {
		case 1:
			this.eng.setText("10");
			break;
		case 2:
			this.itv.setText("1");
			break;
		case 3:
			try {
				this.pw = Float.parseFloat(this.eng.getText());
			} catch (Exception e) {
				this.eng.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if (this.pw <= 0) {
				this.eng.setText(StatCollector.translateToLocal("tof.error"));
				return;

			}
			try {
				this.it = Integer.parseInt(this.itv.getText());
			} catch (Exception e) {
				this.itv.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if (this.it < 1) {
				this.itv.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			PacketDispatcher.sendPacketToServer(PacketHandler.makeInfMJSrcPacket(this.x, this.y, this.z, this.pw, this.it));
			break;
		case 4:
			PacketDispatcher.sendPacketToServer(PacketHandler.makeInfMJSrcAPacket(this.x, this.y, this.z, !this.act));
			break;
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.func_110434_K().func_110577_a(gui);
		int xb = this.width - 176 >> 1;
		int yb = this.height - 214 >> 1;
		drawTexturedModalRect(xb, yb, 0, 0, 176, 214);
		drawCenteredString(this.fontRenderer, "InfinityMJSource", this.width / 2, yb + 6, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, String.format("x:%d, y:%d, z:%d", this.x, this.y, this.z), this.width / 2, yb + 20, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow("Energy(MJ)", this.width / 2 - 70, yb + 39, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow("Interval(tick)", this.width / 2 - 70, yb + 88, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, "1tick=1/20second", this.width / 2, yb + 130, 0xFFFFFF);
		this.eng.drawTextBox();
		this.itv.drawTextBox();
		super.drawScreen(i, j, k);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.eng.mouseClicked(par1, par2, par3);
		this.itv.mouseClicked(par1, par2, par3);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (this.eng.isFocused()) {
			this.eng.textboxKeyTyped(par1, par2);
		} else if (this.itv.isFocused()) {
			this.itv.textboxKeyTyped(par1, par2);
		}
		if (par2 == 1 || par1 == this.mc.gameSettings.keyBindInventory.keyCode) {
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.eng.updateCursorCounter();
		this.itv.updateCursorCounter();
		if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
			this.mc.thePlayer.closeScreen();
		}
	}
}
