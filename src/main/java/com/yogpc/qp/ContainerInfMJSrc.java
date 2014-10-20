package com.yogpc.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.IPacketContainer;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopPacket;

public class ContainerInfMJSrc extends Container implements IPacketContainer {
  private final TileInfMJSrc tile;

  public ContainerInfMJSrc(final TileInfMJSrc t) {
    this.tile = t;
  }

  @Override
  public boolean canInteractWith(final EntityPlayer ep) {
    return ep.getDistanceSq(this.tile.xCoord + 0.5D, this.tile.yCoord + 0.5D,
        this.tile.zCoord + 0.5D) <= 64.0D;
  }

  private float pp;
  private int pi;

  @Override
  public void detectAndSendChanges() {
    super.detectAndSendChanges();
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      if (this.tile.power != this.pp) {
        this.pp = this.tile.power;
        dos.writeByte(1);
        dos.writeFloat(this.tile.power);
      }
      if (this.tile.interval != this.pi) {
        this.pi = this.tile.interval;
        dos.writeByte(2);
        dos.writeInt(this.tile.interval);
      }
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
    try {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final DataOutputStream dos = new DataOutputStream(bos);
      dos.writeByte(1);
      dos.writeFloat(this.tile.power);
      dos.writeByte(2);
      dos.writeInt(this.tile.interval);
      dos.writeByte(-1);
      PacketHandler.sendPacketToPlayer(new YogpstopPacket(this, bos.toByteArray()),
          (EntityPlayer) p);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void receivePacket(final byte[] ba) {
    final ByteArrayDataInput bb = ByteStreams.newDataInput(ba);
    byte slot;
    while ((slot = bb.readByte()) >= 0)
      if (slot == 1)
        this.tile.power = bb.readFloat();
      else if (slot == 2)
        this.tile.interval = bb.readInt();
    return;
  }
}
