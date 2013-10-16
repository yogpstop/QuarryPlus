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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fluids.FluidRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_SlotBlockList extends GuiSlot {
	private static final List<String> blocklist_s = new ArrayList<String>();
	private final List<String> blocklist = new ArrayList<String>(blocklist_s);
	private GuiScreen parent;
	public String current;

	static {
		for (String s : FluidRegistry.getRegisteredFluidIDs().keySet())
			blocklist_s.add(s);
	}

	public GuiP_SlotBlockList(Minecraft par1Minecraft, int par2, int par3, int par4, int par5, int par6, GuiScreen parents, List<String> list) {
		super(par1Minecraft, par2, par3, par4, par5, par6);
		for (int i = 0; i < this.blocklist.size(); i++) {
			for (int j = 0; j < list.size(); j++) {
				if (list.get(j).equals(this.blocklist.get(i))) {
					this.blocklist.remove(i);
					i--;
					if (i < 0) break;
					continue;
				}
			}
		}
		this.parent = parents;
	}

	@Override
	protected int getSize() {
		return this.blocklist.size();
	}

	@Override
	protected void elementClicked(int var1, boolean var2) {
		this.current = this.blocklist.get(var1);
	}

	@Override
	protected boolean isSelected(int var1) {
		return this.blocklist.get(var1).equals(this.current);
	}

	@Override
	protected void drawBackground() {
		this.parent.drawDefaultBackground();
	}

	@Override
	protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5) {
		String name = FluidRegistry.getFluid(this.blocklist.get(var1)).getLocalizedName();
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, (this.parent.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
				var3 + 1, 0xFFFFFF);
	}
}
