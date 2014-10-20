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
import net.minecraftforge.fluids.FluidRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiP_SlotList extends GuiSlot {
  private final GuiScreen parent;
  public int currentore = 0;
  protected List<String> target;

  public GuiP_SlotList(final Minecraft par1Minecraft, final int par2, final int par3,
      final int par4, final int par5, final GuiScreen parents, final List<String> ali) {
    super(par1Minecraft, par2, par3, par4, par5, 18);
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
  protected void drawSlot(final int i, final int v2, final int v3, final int v4,
      final Tessellator t, final int v6, final int v7) {
    String name = this.target.get(i);
    if (FluidRegistry.isFluidRegistered(name))
      name = FluidRegistry.getFluid(name).getLocalizedName(FluidRegistry.getFluidStack(name, 0));
    Minecraft.getMinecraft().fontRenderer
        .drawStringWithShadow(
            name,
            (this.parent.width * 3 / 5 - Minecraft.getMinecraft().fontRenderer.getStringWidth(name)) / 2,
            v3 + 2, 0xFFFFFF);
  }
}
