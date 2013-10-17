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

import org.yogpstop.qp.PacketHandler;
import org.yogpstop.qp.TileBasic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StatCollector;
import static org.yogpstop.qp.QuarryPlus.getname;
import static org.yogpstop.qp.QuarryPlus.data;

@SideOnly(Side.CLIENT)
public class GuiQ_Manual extends GuiScreenA {
	private GuiTextField blockid;
	private GuiTextField meta;
	private byte targetid;
	private TileBasic tile;
	private short bid;
	private int metaid;

	public GuiQ_Manual(GuiScreen parents, byte id, TileBasic tq) {
		super(parents);
		this.targetid = id;
		this.tile = tq;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(-1, this.width / 2 - 150, this.height - 26, 140, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(-2, this.width / 2 + 10, this.height - 26, 140, 20, StatCollector.translateToLocal("gui.cancel")));
		this.blockid = new GuiTextField(this.fontRenderer, this.width / 2 - 50, 50, 100, 20);
		this.meta = new GuiTextField(this.fontRenderer, this.width / 2 - 50, 80, 100, 20);
		this.blockid.setFocused(true);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			try {
				this.bid = Short.parseShort(this.blockid.getText());
			} catch (Exception e) {
				this.blockid.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			try {
				if (this.meta.getText().equals("")) this.metaid = 0;
				else this.metaid = Integer.parseInt(this.meta.getText());
			} catch (Exception e) {
				this.meta.setText(StatCollector.translateToLocal("tof.error"));
				return;
			}
			if ((this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList).contains(data(this.bid, this.metaid))) {
				this.mc.displayGuiScreen(new GuiError(this, StatCollector.translateToLocal("tof.alreadyerror"), getname(this.bid, this.metaid)));
				return;
			}
			this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("tof.addblocksure"), getname(this.bid, this.metaid), -1));
			break;
		case -2:
			showParent();
			break;
		}
	}

	@Override
	public void confirmClicked(boolean par1, int par2) {
		if (par1) PacketHandler.sendPacketToServer(this.tile, (byte) (PacketHandler.CtS_ADD_FORTUNE + this.targetid), data(this.bid, this.metaid));
		else showParent();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (this.blockid.isFocused()) {
			this.blockid.textboxKeyTyped(par1, par2);
		} else if (this.meta.isFocused()) {
			this.meta.textboxKeyTyped(par1, par2);
		}
		super.keyTyped(par1, par2);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.blockid.mouseClicked(par1, par2, par3);
		this.meta.mouseClicked(par1, par2, par3);
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, StatCollector.translateToLocal("tof.selectblock"), this.width / 2, 8, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("tof.blockid"),
				this.width / 2 - 60 - this.fontRenderer.getStringWidth(StatCollector.translateToLocal("tof.blockid")), 50, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("tof.meta"),
				this.width / 2 - 60 - this.fontRenderer.getStringWidth(StatCollector.translateToLocal("tof.meta")), 80, 0xFFFFFF);
		this.fontRenderer.drawString(StatCollector.translateToLocal("tof.tipsmeta"), 16, 110, 0xFFFFFF);
		this.blockid.drawTextBox();
		this.meta.drawTextBox();
		super.drawScreen(i, j, k);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.meta.updateCursorCounter();
		this.blockid.updateCursorCounter();
	}
}
