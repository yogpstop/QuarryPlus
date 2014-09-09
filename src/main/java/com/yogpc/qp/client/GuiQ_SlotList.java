/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQ_SlotList extends GuiSlot {
  private final GuiScreen parent;
  public int currentore = 0;
  protected List<BlockData> target;

  public GuiQ_SlotList(final Minecraft par1Minecraft, final int par2, final int par3,
      final int par4, final int par5, final int par6, final GuiScreen parents,
      final List<BlockData> ali) {
    super(par1Minecraft, par2, par3, par4, par5, par6);
    this.parent = parents;
    this.target = ali;
  }

  @Override
  protected int getSize() {
    return this.target.size();
  }

  @Override
  protected void elementClicked(final int var1, final boolean var2, final int var3, final int var4) {
    this.currentore = var1;
  }

  @Override
  protected boolean isSelected(final int var1) {
    return var1 == this.currentore;
  }

  @Override
  protected void drawBackground() {
    this.parent.drawDefaultBackground();
  }

  @Override
  protected void drawSlot(final int var1, final int var2, final int var3, final int var4,
      final Tessellator var5, final int var6, final int var7) {
    final String name = QuarryPlus.getname(this.target.get(var1));
    Minecraft.getMinecraft().fontRenderer
        .drawStringWithShadow(
            name,
            (this.parent.width * 3 / 5 - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
            var3 + 1, 0xFFFFFF);
  }
}
