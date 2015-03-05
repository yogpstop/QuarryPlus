package com.yogpc.qp.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StatCollector;

public class GuiSlotEntityList extends GuiSlot {
  private final Minecraft mc;
  private final GuiController gc;
  int selected;

  public GuiSlotEntityList(final Minecraft m, final int w, final int h, final int t, final int b,
      final GuiController g) {
    super(m, w, h, t, b, 18);
    this.mc = m;
    this.gc = g;
  }

  @Override
  protected int getSize() {
    return this.gc.list.size();
  }

  @Override
  protected void elementClicked(final int i, final boolean b, final int t, final int l) {
    this.selected = i;
  }

  @Override
  protected boolean isSelected(final int i) {
    return this.selected == i;
  }

  @Override
  protected void drawBackground() {}

  @Override
  protected void drawSlot(final int i, final int v2, final int v3, final int v4,
      final Tessellator t, final int v6, final int v7) {
    final String name = StatCollector.translateToLocal("entity." + this.gc.list.get(i) + ".name");
    Minecraft.getMinecraft().fontRenderer
        .drawStringWithShadow(name,
            (this.mc.currentScreen.width - Minecraft.getMinecraft().fontRenderer
                .getStringWidth(name)) / 2, v3 + 2, 0xFFFFFF);
  }

}
