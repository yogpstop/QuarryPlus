package com.yogpc.qp.container;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.qp.PacketHandler;
import com.yogpc.qp.QuarryPlusI.BlockData;
import com.yogpc.qp.YogpstopPacket;
import com.yogpc.qp.tile.TileBasic;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerEnchList extends Container implements IPacketContainer {
  private final TileBasic tile;

  public ContainerEnchList(final TileBasic tq) {
    this.tile = tq;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer ep) {
    return ep.getDistanceSq(this.tile.xCoord + 0.5D, this.tile.yCoord + 0.5D,
        this.tile.zCoord + 0.5D) <= 64.0D;
  }


  private final List<BlockData> fl = new ArrayList<BlockData>();
  private final List<BlockData> sl = new ArrayList<BlockData>();
  private byte inc;
  private static final byte ADD_FORTUNE = 0;
  private static final byte ADD_SILKTOUCH = 1;
  private static final byte DEL_FORTUNE = 2;
  private static final byte DEL_SILKTOUCH = 3;
  private static final byte CLEAR_ALL = 4;

  private static void writeList(final List<BlockData> l, final byte d, final DataOutputStream o)
      throws IOException {
    for (final BlockData i : l) {
      o.writeByte(d);
      o.writeUTF(i.name);
      o.writeInt(i.meta);
    }
  }

  private static void writeDiff(final List<BlockData> a, final List<BlockData> b, final byte d1,
      final byte d2, final DataOutputStream o) throws IOException {
    List<BlockData> c = new ArrayList<BlockData>(a);
    c.removeAll(b);
    writeList(c, d1, o);
    c = new ArrayList<BlockData>(b);
    c.removeAll(a);
    writeList(c, d2, o);
  }

  private byte getInclude() {
    return (byte) ((this.tile.fortuneInclude ? 2 : 0) | (this.tile.silktouchInclude ? 1 : 0));
  }

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    final byte ninc = getInclude();
    if (this.inc != ninc) {
      this.inc = ninc;
      for (int j = 0; j < this.crafters.size(); ++j)
        ((ICrafting) this.crafters.get(j)).sendProgressBarUpdate(this, 0, this.inc);
    }
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      writeDiff(this.fl, this.tile.fortuneList, DEL_FORTUNE, ADD_FORTUNE, dos);
      writeDiff(this.sl, this.tile.silktouchList, DEL_SILKTOUCH, ADD_SILKTOUCH, dos);
      this.fl.clear();
      this.sl.clear();
      this.fl.addAll(this.tile.fortuneList);
      this.sl.addAll(this.tile.silktouchList);
      if (dos.size() > 0) {
        dos.writeByte(-1);
        for (int j = 0; j < this.crafters.size(); ++j)
          PacketHandler.sendPacketToPlayer(new YogpstopPacket(this, bos.toByteArray()),
              (EntityPlayer) this.crafters.get(j));
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addCraftingToCrafters(final ICrafting p) {
    super.addCraftingToCrafters(p);
    p.sendProgressBarUpdate(this, 0, getInclude());
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      dos.writeByte(CLEAR_ALL);
      writeList(this.fl, ADD_FORTUNE, dos);
      writeList(this.sl, ADD_SILKTOUCH, dos);
      dos.writeByte(-1);
      PacketHandler.sendPacketToPlayer(new YogpstopPacket(this, bos.toByteArray()),
          (EntityPlayer) p);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void updateProgressBar(final int i, final int j) {
    if (i == 0) {
      this.tile.fortuneInclude = (j & 2) != 0;
      this.tile.silktouchInclude = (j & 1) != 0;
    }
  }

  @Override
  public void receivePacket(final byte[] ba) {
    final ByteArrayDataInput bb = ByteStreams.newDataInput(ba);
    byte slot;
    while ((slot = bb.readByte()) >= 0)
      if (slot == ADD_FORTUNE)
        this.tile.fortuneList.add(new BlockData(bb.readUTF(), bb.readInt()));
      else if (slot == ADD_SILKTOUCH)
        this.tile.silktouchList.add(new BlockData(bb.readUTF(), bb.readInt()));
      else if (slot == DEL_FORTUNE)
        this.tile.fortuneList.remove(new BlockData(bb.readUTF(), bb.readInt()));
      else if (slot == DEL_SILKTOUCH)
        this.tile.silktouchList.remove(new BlockData(bb.readUTF(), bb.readInt()));
      else if (slot == CLEAR_ALL) {
        this.tile.silktouchList.clear();
        this.tile.fortuneList.clear();
      }
    return;
  }
}
