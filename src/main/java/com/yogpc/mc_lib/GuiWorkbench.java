package com.yogpc.mc_lib;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {
  private static final ResourceLocation gui = new ResourceLocation("yogpstop_qp",
      "textures/gui/workbench.png");
  private final TileWorkbench tile;

  public GuiWorkbench(final IInventory pi, final TileWorkbench tw) {
    super(new ContainerWorkbench(pi, tw));
    this.xSize = 176;
    this.ySize = 222;
    this.tile = tw;
  }

  @Override
  protected void drawGuiContainerForegroundLayer(final int p_146979_1_, final int p_146979_2_) {
    this.fontRendererObj.drawString(
        this.tile.hasCustomInventoryName() ? this.tile.getInventoryName() : StatCollector
            .translateToLocal(this.tile.getInventoryName()), 8, 6, 0x404040);
    this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8,
        this.ySize - 96 + 2, 0x404040);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(final float p_146976_1_, final int p_146976_2_,
      final int p_146976_3_) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(gui);
    final int xf = this.width - this.xSize >> 1;
    final int yf = this.height - this.ySize >> 1;
    drawTexturedModalRect(xf, yf, 0, 0, this.xSize, this.ySize);
    if (this.tile.cur_recipe >= 0) {
      drawTexturedModalRect(xf + 8, yf + 78, 0, this.ySize, this.tile.cpower, 4);
      drawTexturedModalRect(xf + 8 + this.tile.cur_recipe % 9 * 18, yf + 90
          + (this.tile.cur_recipe / 9 - 3) * 18, this.xSize, 0, 16, 16);
    }
  }
}
