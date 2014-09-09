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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.StatCollector;

import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopPacket;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlus.BlockData;
import com.yogpc.qp.TileBasic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQ_List extends GuiScreenA implements GuiYesNoCallback {
  private GuiQ_SlotList oreslot;
  private GuiButton delete;
  private final TileBasic tile;
  private final byte targetid;

  public GuiQ_List(final byte id, final TileBasic tq) {
    super(null);
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
    this.buttonList.add(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20,
        StatCollector.translateToLocal("gui.done")));
    this.buttonList.add(new GuiButton(PacketHandler.CtS_TOGGLE_FORTUNE + this.targetid,
        this.width * 2 / 3 + 10, 140, 100, 20, StatCollector
            .translateToLocal(include() ? "tof.include" : "tof.exclude")));
    this.buttonList.add(this.delete =
        new GuiButton(PacketHandler.CtS_REMOVE_FORTUNE + this.targetid, this.width * 2 / 3 + 10,
            110, 100, 20, StatCollector.translateToLocal("selectServer.delete")));
    this.oreslot =
        new GuiQ_SlotList(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18, this,
            this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList);
  }

  @Override
  public void actionPerformed(final GuiButton par1) {
    switch (par1.id) {
      case -1:
        showParent();
        break;
      case PacketHandler.CtS_REMOVE_FORTUNE:
      case PacketHandler.CtS_REMOVE_SILKTOUCH:
        this.mc.displayGuiScreen(new GuiYesNo(this, StatCollector
            .translateToLocal("tof.deleteblocksure"), QuarryPlus
            .getname((this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList)
                .get(this.oreslot.currentore)), par1.id));
        break;
      default:
        PacketHandler.sendPacketToServer(this.tile, (byte) par1.id);
        break;
    }
  }

  @Override
  public void drawScreen(final int i, final int j, final float k) {
    drawDefaultBackground();
    this.oreslot.drawScreen(i, j, k);
    drawCenteredString(
        this.fontRendererObj,
        StatCollector.translateToLocal("qp.list.setting")
            + StatCollector.translateToLocal(this.targetid == 0 ? "enchantment.lootBonusDigger"
                : "enchantment.untouching"), this.width / 2, 8, 0xFFFFFF);
    if ((this.targetid == 0 ? this.tile.fortuneList : this.tile.silktouchList).isEmpty())
      this.delete.enabled = false;
    super.drawScreen(i, j, k);
  }

  @Override
  public void confirmClicked(final boolean par1, final int par2) {
    if (par1) {
      final BlockData bd = this.oreslot.target.get(this.oreslot.currentore);
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      try {
        dos.writeUTF(bd.name);
        dos.writeInt(bd.meta);
      } catch (final Exception e) {
        e.printStackTrace();
      }
      PacketHandler
          .sendPacketToServer(new YogpstopPacket(bos.toByteArray(), this.tile, (byte) par2));
    } else
      this.mc.displayGuiScreen(this);
  }
}
