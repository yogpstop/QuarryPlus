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

package com.yogpc.qp.gui;

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
  private final GuiScreen parent;
  public String current;

  static {
    for (final String s : FluidRegistry.getRegisteredFluidIDs().keySet())
      blocklist_s.add(s);
  }

  public GuiP_SlotBlockList(final Minecraft par1Minecraft, final int par2, final int par3,
      final int par4, final int par5, final GuiScreen parents, final List<String> list) {
    super(par1Minecraft, par2, par3, par4, par5, 18);
    for (int i = 0; i < this.blocklist.size(); i++)
      for (int j = 0; j < list.size(); j++)
        if (list.get(j).equals(this.blocklist.get(i))) {
          this.blocklist.remove(i);
          i--;
          if (i < 0)
            break;
          continue;
        }
    this.parent = parents;
  }

  @Override
  protected int getSize() {
    return this.blocklist.size();
  }

  @Override
  protected void elementClicked(final int var1, final boolean var2, final int var3, final int var4) {
    this.current = this.blocklist.get(var1);
  }

  @Override
  protected boolean isSelected(final int var1) {
    return this.blocklist.get(var1).equals(this.current);
  }

  @Override
  protected void drawBackground() {
    this.parent.drawDefaultBackground();
  }

  @Override
  protected void drawSlot(final int i, final int v2, final int v3, final int v4,
      final Tessellator t, final int v6, final int v7) {
    final String name =
        FluidRegistry.getFluid(this.blocklist.get(i)).getLocalizedName(
            FluidRegistry.getFluidStack(this.blocklist.get(i), 0));
    Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(name,
        (this.parent.width - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
        v3 + 2, 0xFFFFFF);
  }
}
