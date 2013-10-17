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
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;

@SideOnly(Side.CLIENT)
public class GuiP_Manual extends GuiScreenA {
	private GuiTextField blockid;
	private byte targetid;
	private TilePump tile;

	public GuiP_Manual(GuiScreen parents, byte id, TilePump tq) {
		super(parents);
		this.targetid = id;
		this.tile = tq;
	}

	@Override
	public void initGui() {
		this.buttonList.add(new GuiButton(-1, this.width / 2 - 150, this.height - 26, 140, 20, StatCollector.translateToLocal("gui.done")));
		this.buttonList.add(new GuiButton(-2, this.width / 2 + 10, this.height - 26, 140, 20, StatCollector.translateToLocal("gui.cancel")));
		this.blockid = new GuiTextField(this.fontRenderer, this.width / 2 - 50, 50, 100, 20);
		this.blockid.setFocused(true);
	}

	@Override
	public void actionPerformed(GuiButton par1) {
		switch (par1.id) {
		case -1:
			String name = this.blockid.getText();
			if (name.length() == 0) return;
			if (this.tile.mapping[this.targetid].contains(name)) {
				if (FluidRegistry.isFluidRegistered(name)) name = FluidRegistry.getFluid(name).getLocalizedName();
				this.mc.displayGuiScreen(new GuiError(this, StatCollector.translateToLocal("tof.alreadyerror"), name));
				return;
			}
			if (FluidRegistry.isFluidRegistered(name)) name = FluidRegistry.getFluid(name).getLocalizedName();
			this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector.translateToLocal("tof.addfluidsure"), name, -1));
			break;
		case -2:
			showParent();
			break;
		}
	}

	@Override
	public void confirmClicked(boolean par1, int par2) {
		if (par1) PacketHandler.sendPacketToServer(this.tile, PacketHandler.CtS_ADD_MAPPING, this.targetid, this.blockid.getText());
		else showParent();
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (this.blockid.isFocused()) {
			this.blockid.textboxKeyTyped(par1, par2);
		}
		super.keyTyped(par1, par2);
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		this.blockid.mouseClicked(par1, par2, par3);
	}

	@Override
	public void drawScreen(int i, int j, float k) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, StatCollector.translateToLocal("tof.selectfluid"), this.width / 2, 8, 0xFFFFFF);
		this.fontRenderer.drawStringWithShadow(StatCollector.translateToLocal("tof.fluidid"),
				this.width / 2 - 60 - this.fontRenderer.getStringWidth(StatCollector.translateToLocal("tof.fluidid")), 50, 0xFFFFFF);
		this.blockid.drawTextBox();
		super.drawScreen(i, j, k);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.blockid.updateCursorCounter();
	}
}
