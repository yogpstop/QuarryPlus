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
import net.minecraft.util.StatCollector;

import static org.yogpstop.qp.QuarryPlus.getname;

@SideOnly(Side.CLIENT)
public class GuiList extends GuiScreen {
	private GuiSlotList oreslot;
	private GuiButton delete;
	private TileBasic tile;
	private byte targetid;

	public GuiList(byte id, TileBasic tq) {
		super();
		this.targetid = id;
		this.tile = tq;
	}

	public boolean include() {
		if (this.targetid == 0) return this.tile.fortuneInclude;
		return this.tile.silktouchInclude;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(PacketHandler.fortuneTInc + this.targetid, this.width * 2 / 3 + 10, 140, 100, 20, StatCollector
				.translateToLocal(include() ? "tof.include" : "tof.exclude")));
		this.buttonList.add(new GuiButton(-2, this.width * 2 / 3 + 10, 80, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.manualinput") + ")"));
		this.buttonList.add(new GuiButton(-3, this.width * 2 / 3 + 10, 50, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.fromlist") + ")"));
		this.buttonList.add(this.delete = new GuiButton(PacketHandler.fortuneRemove + this.targetid, this.width * 2 / 3 + 10, 110, 100, 20, StatCollector
				.translateToLocal("selectServer.delete")));
		this.oreslot = new GuiSlotList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this, this.targetid == 0 ? this.tile.fortuneList
				: this.tile.silktouchList);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			this.mc.displayGuiScreen(null);
			break;
		case -2:
			this.mc.displayGuiScreen(new GuiManual(this, this.targetid, this.tile));
			break;
		case -3:
			this.mc.displayGuiScreen(new GuiSelectBlock(this, this.tile, this.targetid));
			break;
		case PacketHandler.fortuneRemove:
		case PacketHandler.silktouchRemove:
			this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("tof.deleteblocksure"),
					getname((this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList).get(this.oreslot.currentore)), par1.id));
			break;
		default:
			PacketHandler.sendPacketToServer(this.tile, (byte) par1.id);
			break;
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		this.drawDefaultBackground();
		this.oreslot.drawScreen(i, j, k);
		this.drawCenteredString(
				this.fontRenderer,
				StatCollector.translateToLocal("qp.list.setting")
						+ StatCollector.translateToLocal(this.targetid == 0 ? "enchantment.lootBonusDigger" : "enchantment.untouching"), this.width / 2, 8,
				0xFFFFFF);
		if ((this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList).isEmpty()) {
			this.delete.enabled = false;
		}
		super.drawScreen(i, j, k);
	}

	@Override
	public void confirmClicked(boolean par1, int par2) {
		if (par1) {
			PacketHandler.sendPacketToServer(this.tile, (byte) par2, this.oreslot.target.get(this.oreslot.currentore));
		}
		this.mc.displayGuiScreen(this);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead) {
			this.mc.thePlayer.closeScreen();
		}
	}
}
