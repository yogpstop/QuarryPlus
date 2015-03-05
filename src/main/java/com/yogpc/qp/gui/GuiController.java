package com.yogpc.qp.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;

import com.yogpc.qp.PacketHandler;
import com.yogpc.qp.YogpstopPacket;
import com.yogpc.qp.block.BlockController;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiController extends GuiScreen {
  private GuiSlotEntityList slot;
  final List<String> list;
  final int dim, xc, yc, zc;

  public GuiController(final int d, final int x, final int y, final int z, final List<String> l) {
    this.list = l;
    this.dim = d;
    this.xc = x;
    this.yc = y;
    this.zc = z;
  }

  @Override
  public void initGui() {
    super.initGui();
    this.buttonList.add(new GuiButton(-1, this.width / 2 - 125, this.height - 26, 250, 20,
        StatCollector.translateToLocal("gui.done")));
    this.slot =
        new GuiSlotEntityList(this.mc, this.width, this.height - 60, 30, this.height - 30, this);
  }

  @Override
  public void actionPerformed(final GuiButton par1) {
    switch (par1.id) {
      case -1:
        try {
          final ByteArrayOutputStream bos = new ByteArrayOutputStream();
          final DataOutputStream dos = new DataOutputStream(bos);
          dos.writeByte(0);
          dos.writeInt(this.dim);
          dos.writeInt(this.xc);
          dos.writeInt(this.yc);
          dos.writeInt(this.zc);
          dos.writeUTF(this.list.get(this.slot.selected));
          PacketHandler.sendPacketToServer(new YogpstopPacket(bos.toByteArray(),
              BlockController.class));
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
        this.mc.thePlayer.closeScreen();
        break;
    }
  }

  @Override
  public void drawScreen(final int i, final int j, final float k) {
    this.slot.drawScreen(i, j, k);
    super.drawScreen(i, j, k);
    drawCenteredString(this.fontRendererObj, StatCollector.translateToLocal("yog.ctl.setting"),
        this.width / 2, 8, 0xFFFFFF);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    if (!this.mc.thePlayer.isEntityAlive() || this.mc.thePlayer.isDead)
      this.mc.thePlayer.closeScreen();
  }

  @Override
  protected void keyTyped(final char p_73869_1_, final int p_73869_2_) {
    if (p_73869_2_ == 1 || p_73869_2_ == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
      this.mc.displayGuiScreen(null);
      this.mc.setIngameFocus();
    }
  }
}
