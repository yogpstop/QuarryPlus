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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.yogpc.qp.PacketHandler;
import com.yogpc.qp.YogpstopPacket;
import com.yogpc.qp.container.ContainerMover;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMover extends GuiContainer {
  private static final ResourceLocation gui = new ResourceLocation("yogpstop_qp",
      "textures/gui/mover.png");

  public GuiMover(final EntityPlayer player, final World world, final int x, final int y,
      final int z) {
    super(new ContainerMover(player.inventory, world, x, y, z));
  }

  @Override
  public void initGui() {
    super.initGui();
    final int i = this.width - this.xSize >> 1;
    final int j = this.height - this.ySize >> 1;
    this.buttonList.add(new GuiButton(1, i + 27, j + 18, 122, 18, "↑"));
    this.buttonList.add(new GuiButton(2, i + 27, j + 36, 122, 18, ""));
    this.buttonList.add(new GuiButton(3, i + 27, j + 54, 122, 18, "↓"));
  }

  @Override
  protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
    this.fontRendererObj.drawString(StatCollector.translateToLocal("tile.EnchantMover.name"), 8, 6,
        0x404040);
    this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 72,
        0x404040);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(gui);
    drawTexturedModalRect(this.width - this.xSize >> 1, this.height - this.ySize >> 1, 0, 0,
        this.xSize, this.ySize);
  }

  @Override
  protected void actionPerformed(final GuiButton par1GuiButton) {
    if (!par1GuiButton.enabled)
      return;
    PacketHandler.sendPacketToServer(new YogpstopPacket(this.inventorySlots,
        new byte[] {(byte) par1GuiButton.id}));
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    final int eid = ((ContainerMover) this.inventorySlots).avail;
    ((GuiButton) this.buttonList.get(1)).displayString =
        eid >= 0 ? StatCollector.translateToLocal(Enchantment.enchantmentsList[eid].getName()) : "";
  }
}
