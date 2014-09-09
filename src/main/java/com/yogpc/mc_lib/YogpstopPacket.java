package com.yogpc.mc_lib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.NetworkRegistry;

public final class YogpstopPacket {
  private byte[] header;
  private byte[] data;
  private EntityPlayer ep;

  public YogpstopPacket() {}

  public YogpstopPacket(final TileEntity o) {
    final NBTTagCompound nbttc = new NBTTagCompound();
    o.writeToNBT(nbttc);
    try {
      this.data = CompressedStreamTools.compress(nbttc);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.header = new byte[] {PacketHandler.NBT};
  }

  public YogpstopPacket(final int c) {
    this.data = new byte[] {(byte) (c >>> 24), (byte) (c >>> 16), (byte) (c >>> 8), (byte) c};
    this.header = new byte[] {PacketHandler.KEY};
  }

  public YogpstopPacket(final byte d) {
    this.data = new byte[] {d};
    this.header = new byte[] {PacketHandler.BTN};
  }

  public YogpstopPacket(final byte[] d, final Class<?> o) {
    this.data = d;
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream(bos);
    try {
      dos.write(PacketHandler.STATIC);
      dos.writeUTF(((Class<?>) o).getName());
      this.header = bos.toByteArray();
      dos.close();
      bos.close();
    } catch (final IOException e) {
    }
  }

  public YogpstopPacket(final byte[] d, final TileEntity o, final byte i) {
    this.data = d;
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream(bos);
    try {
      dos.write(PacketHandler.Tile);
      dos.writeInt(o.xCoord);
      dos.writeInt(o.yCoord);
      dos.writeInt(o.zCoord);
      dos.write(i);
      this.header = bos.toByteArray();
      dos.close();
      bos.close();
    } catch (final IOException e) {
    }
  }

  public EntityPlayer getPlayer() {
    return this.ep;
  }

  public byte getChannel() {
    return this.header[0];
  }

  public byte[] getHeader() {
    final byte[] r = new byte[this.header.length - 1];
    System.arraycopy(this.header, 1, r, 0, this.header.length - 1);
    return r;
  }

  public byte[] getData() {
    return this.data;
  }

  public void readData(final ByteBuf d, final ChannelHandlerContext ctx) {
    this.ep =
        YogpstopLib.proxy.getPacketPlayer(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
    this.header = new byte[d.readInt()];
    d.readBytes(this.header);
    this.data = new byte[d.readableBytes()];
    d.readBytes(this.data);
  }

  public void writeData(final ByteBuf d) {
    d.writeInt(this.header.length);
    d.writeBytes(this.header);
    d.writeBytes(this.data);
  }
}
