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

import static org.yogpstop.qp.QuarryPlus.getname;
import static org.yogpstop.qp.QuarryPlus.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQ_SlotBlockList extends GuiSlot {
	private static final List<ItemStack> blocklist_s = new ArrayList<ItemStack>();
	private final List<ItemStack> blocklist = new ArrayList<ItemStack>(blocklist_s);
	private GuiScreen parent;
	public short currentblockid;
	public int currentmeta;

	static {
		for (int i = 0; i < 4096; i++) {
			if (Block.blocksList[i] != null) {
				Block.blocksList[i].getSubBlocks(i, null, blocklist_s);
			}
		}
	}

	public GuiQ_SlotBlockList(Minecraft par1Minecraft, int par2, int par3, int par4, int par5, int par6, GuiScreen parents, List<Long> list) {
		super(par1Minecraft, par2, par3, par4, par5, par6);
		for (int i = 0; i < this.blocklist.size(); i++) {
			for (int j = 0; j < list.size(); j++) {
				if (data((short) this.blocklist.get(i).itemID, this.blocklist.get(i).getItemDamage()) == list.get(j)) {
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
		this.currentblockid = (short) this.blocklist.get(var1).itemID;
		this.currentmeta = this.blocklist.get(var1).getItemDamage();
	}

	@Override
	protected boolean isSelected(int var1) {
		return this.blocklist.get(var1).itemID == this.currentblockid && this.currentmeta == this.blocklist.get(var1).getItemDamage();
	}

	@Override
	protected void drawBackground() {
		this.parent.drawDefaultBackground();
	}

	@Override
	protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5) {
		String name = getname((short) this.blocklist.get(var1).itemID, this.blocklist.get(var1).getItemDamage());
		Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name, (this.parent.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
				var3 + 1, 0xFFFFFF);
	}
}
