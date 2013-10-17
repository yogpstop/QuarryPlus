/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TileInfMJSrc;

@SideOnly(Side.CLIENT)
public class GuiInfMJSrc extends GuiScreenA {
	private static final ResourceLocation gui = new ResourceLocation("yogpstop_qp", "textures/gui/infmjsrc.png");
	private TileInfMJSrc tile;
	private GuiTextField eng;
	private GuiTextField itv;

	public GuiInfMJSrc(TileInfMJSrc pt) {
		super(null);
		this.tile = pt;
	}

	@Override
	public void initGui() {
		super.initGui();
		// int xb = (this.width - 176) >> 1;
		int yb = (this.height - 214) >> 1;
		this.eng = new GuiTextField(this.fontRenderer, (this.width >> 1) - 75, yb + 58, 150, 20);
		this.eng.setText(Float.toString(this.tile.power));
		this.itv = new GuiTextField(this.fontRenderer, (this.width >> 1) - 75, yb + 106, 150, 20);
		this.itv.setText(Integer.toString(this.tile.interval));
		this.buttonList.add(new GuiButton(1, (this.width >> 1) + 30, yb + 34, 50, 20, "Reset"));
		this.buttonList.add(new GuiButton(2, (this.width >> 1) + 30, yb + 82, 50, 20, "Reset"));
		this.buttonList.add(new GuiButton(3, (this.width >> 1) - 75, yb + 144, 150, 20, "Apply"));
	}

	@Override
	protected void actionPerformed(GuiButton gb) {
		if (!gb.enabled) { return; }
		switch (gb.id) {
		case 1:
			this.eng.setText("10.0");
			break;
		case 2:
			this.itv.setText("1");
			break;
		case 3:
			try {
				this.tile.power = Float.parseFloat(this.eng.getText());
			} catch (Exception e) {
				this.eng.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if (this.tile.power <= 0) {
				this.eng.setText(StatCollector.translateToLocal("tof.error"));
				return;

			}
			try {
				this.tile.interval = Integer.parseInt(this.itv.getText());
			} catch (Exception e) {
				this.itv.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if (this.tile.interval < 1) {
				this.itv.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			try {
				dos.writeInt(this.tile.xCoord);
				dos.writeInt(this.tile.yCoord);
				dos.writeInt(this.tile.zCoord);
				dos.writeByte(PacketHandler.CtS_INFMJSRC);
				dos.writeFloat(this.tile.power);
				dos.writeInt(this.tile.interval);
			} catch (Exception e) {
				e.printStackTrace();
			}
			PacketDispatcher.sendPacketToServer(PacketHandler.composeTilePacket(bos));
			break;
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(gui);
		int xb = this.width - 176 >> 1;
		int yb = this.height - 214 >> 1;
		drawTexturedModalRect(xb, yb, 0, 0, 176, 214);
		drawCenteredString(this.fontRenderer, StatCollector.translateToLocal("tile.InfMJSrc.name"), this.width / 2, yb + 6, 0xFFFFFF);
		drawCenteredString(this.fontRenderer, String.format("x:%d, y:%d, z:%d", this.tile.xCoord, this.tile.yCoord, this.tile.zCoord), this.width / 2, yb + 20,
				0xFFFFFF);
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
		super.keyTyped(par1, par2);
	}
}
