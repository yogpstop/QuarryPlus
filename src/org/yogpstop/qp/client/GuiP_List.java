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
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;

@SideOnly(Side.CLIENT)
public class GuiP_List extends GuiScreenA {
	private GuiP_SlotList oreslot;
	private GuiButton delete, top, up, down, bottom;
	private TilePump tile;
	byte dir;

	public GuiP_List(byte id, TilePump tq) {
		super(null);
		this.dir = id;
		this.tile = tq;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(-4, this.width / 2 - 160, this.height - 26, 100, 20, StatCollector.translateToLocal("pp.change")));
		this.buttonList.add(new GuiButton(-1, this.width / 2 - 50, this.height - 26, 100, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(-5, this.width / 2 + 60, this.height - 26, 100, 20, StatCollector.translateToLocal("pp.copy")));
		this.buttonList.add(new GuiButton(-2, this.width * 2 / 3 + 10, 45, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.manualinput") + ")"));
		this.buttonList.add(new GuiButton(-3, this.width * 2 / 3 + 10, 20, 100, 20, StatCollector.translateToLocal("tof.addnewore") + "("
				+ StatCollector.translateToLocal("tof.fromlist") + ")"));
		this.buttonList.add(this.delete = new GuiButton(PacketHandler.CtS_REMOVE_MAPPING, this.width * 2 / 3 + 10, 70, 100, 20, StatCollector
				.translateToLocal("selectServer.delete")));
		this.buttonList.add(this.top = new GuiButton(PacketHandler.CtS_TOP_MAPPING, this.width * 2 / 3 + 10, 95, 100, 20, StatCollector
				.translateToLocal("tof.top")));
		this.buttonList.add(this.up = new GuiButton(PacketHandler.CtS_UP_MAPPING, this.width * 2 / 3 + 10, 120, 100, 20, StatCollector
				.translateToLocal("tof.up")));
		this.buttonList.add(this.down = new GuiButton(PacketHandler.CtS_DOWN_MAPPING, this.width * 2 / 3 + 10, 145, 100, 20, StatCollector
				.translateToLocal("tof.down")));
		this.buttonList.add(this.bottom = new GuiButton(PacketHandler.CtS_BOTTOM_MAPPING, this.width * 2 / 3 + 10, 170, 100, 20, StatCollector
				.translateToLocal("tof.bottom")));
		this.oreslot = new GuiP_SlotList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this, this.tile.mapping[this.dir]);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			showParent();
			break;
		case -2:
			this.mc.displayGuiScreen(new GuiP_Manual(this, this.dir, this.tile));
			break;
		case -3:
			this.mc.displayGuiScreen(new GuiP_SelectBlock(this, this.tile, this.dir));
			break;
		case -4:
		case -5:
			this.mc.displayGuiScreen(new GuiP_SelectSide(this.tile, this, par1.id == -5));
			break;
		case PacketHandler.CtS_REMOVE_MAPPING:
			String name = this.tile.mapping[this.dir].get(this.oreslot.currentore);
			if (FluidRegistry.isFluidRegistered(name)) name = FluidRegistry.getFluid(name).getLocalizedName();
			this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("tof.deletefluidsure"), name, par1.id));
			break;
		default:
			PacketHandler.sendPacketToServer(this.tile, (byte) par1.id, this.dir, this.tile.mapping[this.dir].get(this.oreslot.currentore));
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
						+ StatCollector.translateToLocal(TilePump.fdToString(ForgeDirection.getOrientation(this.dir))), this.width / 2, 8, 0xFFFFFF);
		if (this.tile.mapping[this.dir].isEmpty()) {
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
		if (par1) PacketHandler.sendPacketToServer(this.tile, (byte) par2, this.dir, this.oreslot.target.get(this.oreslot.currentore));
		else this.mc.displayGuiScreen(this);
	}
}
