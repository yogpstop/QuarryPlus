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
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.StatCollector;

import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopPacket;
import com.yogpc.qp.ContainerEnchList;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlus.BlockData;
import com.yogpc.qp.TileBasic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnchList extends GuiContainer implements GuiYesNoCallback {
  private GuiSlotEnchList slot;
  private final TileBasic tile;
  private final byte targetid;

  public GuiEnchList(final byte id, final TileBasic tq) {
    super(new ContainerEnchList(tq));
    this.targetid = id;
    this.tile = tq;
  }

  public boolean include() {
    if (this.targetid == 0)
      return this.tile.fortuneInclude;
    return this.tile.silktouchInclude;
  }

  @Override
  public void initGui() {
    this.xSize = this.width;
    this.ySize = this.height;
    super.initGui();
    this.buttonList.add(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20,
        StatCollector.translateToLocal("gui.done")));
    this.buttonList.add(new GuiButton(PacketHandler.CtS_TOGGLE_FORTUNE + this.targetid,
        this.width * 2 / 3 + 10, 140, 100, 20, ""));
    this.buttonList.add(new GuiButton(PacketHandler.CtS_REMOVE_FORTUNE + this.targetid,
        this.width * 2 / 3 + 10, 110, 100, 20, StatCollector
            .translateToLocal("selectServer.delete")));
    this.slot =
        new GuiSlotEnchList(this.mc, this.width * 3 / 5, this.height - 60, 30, this.height - 30,
            this, this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList);
  }

  @Override
  public void actionPerformed(final GuiButton par1) {
    switch (par1.id) {
      case -1:
        this.mc.thePlayer.closeScreen();
        break;
      case PacketHandler.CtS_REMOVE_FORTUNE:
      case PacketHandler.CtS_REMOVE_SILKTOUCH:
        this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector
            .translateToLocal("tof.deleteblocksure"),
            QuarryPlus.getLocalizedName((this.targetid == 0 ? this.tile.fortuneList
                : this.tile.silktouchList).get(this.slot.currentore)), par1.id));
        break;
      default:
        PacketHandler.sendPacketToServer(this.tile, (byte) par1.id);
        break;
    }
  }

  @Override
  public void confirmClicked(final boolean par1, final int par2) {
    if (par1) {
      final BlockData bd = this.slot.target.get(this.slot.currentore);
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      try {
        dos.writeUTF(bd.name);
        dos.writeInt(bd.meta);
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
      PacketHandler
          .sendPacketToServer(new YogpstopPacket(bos.toByteArray(), this.tile, (byte) par2));
    }
    this.mc.displayGuiScreen(this);
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(final float k, final int i, final int j) {
    this.slot.drawScreen(i, j, k);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(final int i, final int j) {
    drawCenteredString(this.fontRendererObj, StatCollector.translateToLocalFormatted(
        "qp.list.setting", StatCollector
            .translateToLocal(this.targetid == 0 ? "enchantment.lootBonusDigger"
                : "enchantment.untouching")), this.xSize / 2, 8, 0xFFFFFF);
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    ((GuiButton) this.buttonList.get(1)).displayString =
        StatCollector.translateToLocal(include() ? "tof.include" : "tof.exclude");
    ((GuiButton) this.buttonList.get(2)).enabled =
        !(this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList).isEmpty();
  }
}
