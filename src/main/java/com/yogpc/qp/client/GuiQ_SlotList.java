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

package com.yogpc.qp.client;

import static com.yogpc.qp.QuarryPlus.getname;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;

@SideOnly(Side.CLIENT)
public class GuiQ_SlotList extends GuiSlot {
	private GuiScreen parent;
	public int currentore = 0;
	protected List<ItemStack> target;

	public GuiQ_SlotList(Minecraft par1Minecraft, int par2, int par3, int par4, int par5, int par6, GuiScreen parents, List<ItemStack> ali) {
		super(par1Minecraft, par2, par3, par4, par5, par6);
		this.parent = parents;
		this.target = ali;
	}

	@Override
	protected int getSize() {
		return this.target.size();
	}

	@Override
	protected void elementClicked(int var1, boolean var2, int var3, int var4) {
		this.currentore = var1;
	}

	@Override
	protected boolean isSelected(int var1) {
		return var1 == this.currentore;
	}

	@Override
	protected void drawBackground() {
		this.parent.drawDefaultBackground();
	}

	@Override
	protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5, int var6, int var7) {
		String name = getname(this.target.get(var1));
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name,
				(this.parent.width * 3 / 5 - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2, var3 + 1, 0xFFFFFF);
	}
}
