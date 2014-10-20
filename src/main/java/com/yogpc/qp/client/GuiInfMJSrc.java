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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopPacket;
import com.yogpc.qp.ContainerInfMJSrc;
import com.yogpc.qp.TileInfMJSrc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInfMJSrc extends GuiContainer {
  private static final ResourceLocation gui = new ResourceLocation("yogpstop_qp",
      "textures/gui/infmjsrc.png");
  private final TileInfMJSrc tile;
  private GuiTextField eng;
  private GuiTextField itv;

  public GuiInfMJSrc(final TileInfMJSrc pt) {
    super(new ContainerInfMJSrc(pt));
    this.tile = pt;
    this.ySize = 214;
  }

  @Override
  public void initGui() {
    super.initGui();
    // int xb = (this.width - 176) >> 1;
    final int yb = this.height - 214 >> 1;
    this.eng = new GuiTextField(this.fontRendererObj, (this.width >> 1) - 75, yb + 58, 150, 20);
    this.eng.setText(Float.toString(this.tile.power));
    this.itv = new GuiTextField(this.fontRendererObj, (this.width >> 1) - 75, yb + 106, 150, 20);
    this.itv.setText(Integer.toString(this.tile.interval));
    this.buttonList.add(new GuiButton(1, (this.width >> 1) + 30, yb + 34, 50, 20, StatCollector
        .translateToLocal("gui.reset")));
    this.buttonList.add(new GuiButton(2, (this.width >> 1) + 30, yb + 82, 50, 20, StatCollector
        .translateToLocal("gui.reset")));
    this.buttonList.add(new GuiButton(3, (this.width >> 1) - 75, yb + 144, 150, 20, StatCollector
        .translateToLocal("gui.apply")));
  }

  @Override
  protected void actionPerformed(final GuiButton gb) {
    if (!gb.enabled)
      return;
    switch (gb.id) {
      case 1:
        this.eng.setText(Float.toString(this.tile.power));
        break;
      case 2:
        this.itv.setText(Integer.toString(this.tile.interval));
        break;
      case 3:
        try {
          this.tile.power = Float.parseFloat(this.eng.getText());
        } catch (final NumberFormatException e) {
          this.eng.setText(StatCollector.translateToLocal("tof.error"));
          return;
        }
        if (this.tile.power <= 0) {
          this.eng.setText(StatCollector.translateToLocal("tof.error"));
          return;

        }
        try {
          this.tile.interval = Integer.parseInt(this.itv.getText());
        } catch (final NumberFormatException e) {
          this.itv.setText(StatCollector.translateToLocal("tof.error"));
          return;
        }
        if (this.tile.interval < 1) {
          this.itv.setText(StatCollector.translateToLocal("tof.error"));
          return;
        }
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        try {
          dos.writeFloat(this.tile.power);
          dos.writeInt(this.tile.interval);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
        PacketHandler.sendPacketToServer(new YogpstopPacket(bos.toByteArray(), this.tile,
            PacketHandler.CtS_INFMJSRC));
        break;
    }
  }

  @Override
  protected void drawGuiContainerForegroundLayer(final int x, final int y) {
    super.drawGuiContainerForegroundLayer(x, y);
    drawCenteredString(this.fontRendererObj, StatCollector.translateToLocal("tile.InfMJSrc.name"),
        this.xSize / 2, 6, 0xFFFFFF);
    final StringBuilder sb = new StringBuilder();
    sb.append("x:").append(this.tile.xCoord).append(" y:").append(this.tile.yCoord).append("z: ")
        .append(this.tile.zCoord);
    drawCenteredString(this.fontRendererObj, sb.toString(), this.xSize / 2, 20, 0xFFFFFF);
    this.fontRendererObj.drawStringWithShadow(
        StatCollector.translateToLocal("gui.infmjsrc.energy"), this.xSize / 2 - 70, 39, 0xFFFFFF);
    this.fontRendererObj.drawStringWithShadow(StatCollector.translateToLocal("gui.infmjsrc.itv"),
        this.xSize / 2 - 70, 88, 0xFFFFFF);
    drawCenteredString(this.fontRendererObj,
        StatCollector.translateToLocal("gui.infmjsrc.tickinfo"), this.xSize / 2, 130, 0xFFFFFF);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(final float f, final int i, final int j) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(gui);
    drawTexturedModalRect(this.width - 176 >> 1, this.height - 214 >> 1, 0, 0, this.xSize,
        this.ySize);
    this.eng.drawTextBox();
    this.itv.drawTextBox();
  }

  @Override
  protected void mouseClicked(final int par1, final int par2, final int par3) {
    super.mouseClicked(par1, par2, par3);
    this.eng.mouseClicked(par1, par2, par3);
    this.itv.mouseClicked(par1, par2, par3);
  }

  @Override
  protected void keyTyped(final char c, final int i) {
    if (this.eng.isFocused())
      this.eng.textboxKeyTyped(c, i);
    else if (this.itv.isFocused())
      this.itv.textboxKeyTyped(c, i);
    else
      super.keyTyped(c, i);
  }

  private float pp;
  private int pi;

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (this.pp != this.tile.power) {
      this.pp = this.tile.power;
      this.eng.setText(Float.toString(this.pp));
    }
    if (this.pi != this.tile.interval) {
      this.pi = this.tile.interval;
      this.itv.setText(Integer.toString(this.pi));
    }
  }
}
