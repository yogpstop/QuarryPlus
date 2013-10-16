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
import org.yogpstop.qp.TilePump;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ForgeDirection;

@SideOnly(Side.CLIENT)
public class GuiP_List extends GuiScreen {
	private GuiP_SlotList oreslot;
	private GuiButton delete, top, up, down, bottom;
	private TilePump tile;
	private byte targetid;

	public GuiP_List(byte id, TilePump tq) {
		super();
		this.targetid = id;
		this.tile = tq;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(-2, this.width * 2 / 3 + 10, 50, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.manualinput") + ")"));
		this.buttonList.add(new GuiButton(-3, this.width * 2 / 3 + 10, 20, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.fromlist") + ")"));
		this.buttonList.add(this.delete = new GuiButton(PacketHandler.CtS_REMOVE_MAPPING, this.width * 2 / 3 + 10, 80, 100, 20, StatCollector
				.translateToLocal("selectServer.delete")));
		this.buttonList.add(this.top = new GuiButton(PacketHandler.CtS_TOP_MAPPING, this.width * 2 / 3 + 10, 110, 100, 20, StatCollector
				.translateToLocal("tof.top")));
		this.buttonList.add(this.up = new GuiButton(PacketHandler.CtS_UP_MAPPING, this.width * 2 / 3 + 10, 140, 100, 20, StatCollector
				.translateToLocal("tof.up")));
		this.buttonList.add(this.down = new GuiButton(PacketHandler.CtS_DOWN_MAPPING, this.width * 2 / 3 + 10, 170, 100, 20, StatCollector
				.translateToLocal("tof.down")));
		this.buttonList.add(this.bottom = new GuiButton(PacketHandler.CtS_BOTTOM_MAPPING, this.width * 2 / 3 + 10, 200, 100, 20, StatCollector
				.translateToLocal("tof.bottom")));
		this.oreslot = new GuiP_SlotList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this, this.tile.mapping[this.targetid]);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			this.mc.displayGuiScreen(null);
			break;
		case -2:
			this.mc.displayGuiScreen(new GuiP_Manual(this, this.targetid, this.tile));
			break;
		case -3:
			this.mc.displayGuiScreen(new GuiP_SelectBlock(this, this.tile, this.targetid));
			break;
		case PacketHandler.CtS_REMOVE_MAPPING:
			this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("tof.deleteblocksure"), this.tile.mapping[this.targetid]
					.get(this.oreslot.currentore), par1.id));
			break;
		default:
			PacketHandler.sendPacketToServer(this.tile, (byte) par1.id, this.targetid, this.tile.mapping[this.targetid].get(this.oreslot.currentore));
			break;
		}
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		this.drawDefaultBackground();
		this.oreslot.drawScreen(i, j, k);
		this.drawCenteredString(
				this.fontRenderer,
				StatCollector.translateToLocal("pp.list.setting")
						+ StatCollector.translateToLocal(TilePump.fdToString(ForgeDirection.getOrientation(this.targetid))), this.width / 2, 8, 0xFFFFFF);
		if (this.tile.mapping[this.targetid].isEmpty()) {
			this.delete.enabled = false;
			this.top.enabled = false;
			this.up.enabled = false;
			this.down.enabled = false;
			this.bottom.enabled = false;
		}
		super.drawScreen(i, j, k);
	}

	@Override
	public void confirmClicked(boolean par1, int par2) {
		if (par1) PacketHandler.sendPacketToServer(this.tile, (byte) par2, this.targetid, this.oreslot.target.get(this.oreslot.currentore));
		else this.mc.displayGuiScreen(this);
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
