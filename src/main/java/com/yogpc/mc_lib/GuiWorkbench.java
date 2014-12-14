package com.yogpc.mc_lib;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {
  private static final class MyFontRenderer extends FontRenderer {
    private FontRenderer p;

    public MyFontRenderer() {
      super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
          Minecraft.getMinecraft().renderEngine, false);
    }

    @Override
    public int drawStringWithShadow(final String s, final int x, final int y, final int color) {
      int l = this.p.getStringWidth(s);
      GL11.glPushMatrix();
      GL11.glTranslatef(x, y, 0);
      if (l > 16) {
        final float f = (float) 16 / l;
        GL11.glTranslatef(l - 16, this.p.FONT_HEIGHT * (1 - f), 0);
        GL11.glScalef(f, f, 1);
      }
      l = this.p.drawStringWithShadow(s, 0, 0, color);
      GL11.glPopMatrix();
      return l;
    }

    @Override
    public int getStringWidth(final String s) {
      return this.p.getStringWidth(s);
    }

    FontRenderer setParent(final FontRenderer r) {
      this.p = r;
      return this;
    }
  }

  private static final class MyRenderItem extends RenderItem {
    private static final MyFontRenderer myfont = new MyFontRenderer();

    public MyRenderItem() {}

    @Override
    public void renderItemOverlayIntoGUI(final FontRenderer a, final TextureManager b,
        final ItemStack c, final int d, final int e, final String f) {
      super.renderItemOverlayIntoGUI(myfont.setParent(a), b, c, d, e, f);
    }
  }

  private static final RenderItem myitem = new MyRenderItem();

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

  private static final Field item = ReflectionHelper.getField(ReflectionHelper.getClass(
      "codechicken.nei.guihook.GuiContainerManager", "codechicken.nei.forge.GuiContainerManager"),
      "drawItems");

  @Override
  public void drawScreen(final int p_73863_1_, final int p_73863_2_, final float p_73863_3_) {
    RenderItem nitem = null;
    if (item != null)
      try {
        nitem = (RenderItem) item.get(null);
        item.set(null, myitem);
      } catch (final Exception e) {
      }
    final RenderItem pitem = GuiScreen.itemRender;
    GuiScreen.itemRender = myitem;
    super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    GuiScreen.itemRender = pitem;
    if (nitem != null)
      try {
        item.set(null, nitem);
      } catch (final Exception e) {
      }
  }
}
