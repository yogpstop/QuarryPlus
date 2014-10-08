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

package com.yogpc.mc_lib;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<YogpstopPacket> {
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Handler {
  }

  private static final Map<String, Method> registeredStaticHandlers = new HashMap<String, Method>();

  public static final void registerStaticHandler(final Class<?> c) {
    final List<Method> l = ReflectionHelper.getMethods(c, Handler.class);
    if (l.size() == 1)
      registeredStaticHandlers.put(c.getName(), l.get(0));
  }

  public static EnumMap<Side, FMLEmbeddedChannel> channels;
  static final byte Tile = 0;
  static final byte NBT = 1;
  static final byte BTN = 2;
  static final byte STATIC = 3;
  static final byte KEY = 4;

  public static final byte StC_OPENGUI_FORTUNE = 0;
  public static final byte StC_OPENGUI_SILKTOUCH = 1;
  public static final byte StC_OPENGUI_INFMJSRC = 2;
  public static final byte StC_OPENGUI_MAPPING = 3;
  public static final byte StC_NOW = 4;
  public static final byte StC_HEAD_POS = 5;
  public static final byte StC_UPDATE_MARKER = 6;
  public static final byte StC_LINK_RES = 7;

  public static final byte CtS_ADD_FORTUNE = 8;
  public static final byte CtS_ADD_SILKTOUCH = 9;
  public static final byte CtS_REMOVE_FORTUNE = 10;
  public static final byte CtS_REMOVE_SILKTOUCH = 11;
  public static final byte CtS_TOGGLE_FORTUNE = 12;
  public static final byte CtS_TOGGLE_SILKTOUCH = 13;
  public static final byte CtS_LINK_REQ = 14;
  public static final byte CtS_INFMJSRC = 15;
  public static final byte CtS_ADD_MAPPING = 16;
  public static final byte CtS_REMOVE_MAPPING = 17;
  public static final byte CtS_UP_MAPPING = 18;
  public static final byte CtS_DOWN_MAPPING = 19;
  public static final byte CtS_TOP_MAPPING = 20;
  public static final byte CtS_BOTTOM_MAPPING = 21;
  public static final byte CtS_RENEW_DIRECTION = 22;
  public static final byte CtS_COPY_MAPPING = 23;

  public static final byte remove_link = 0;
  public static final byte remove_laser = 1;

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final YogpstopPacket packet)
      throws Exception {
    if (packet.getChannel() == NBT)
      setNBTFromPacket(packet);
    else if (packet.getChannel() == BTN) {
      final Container container = packet.getPlayer().openContainer;
      if (container instanceof IPacketContainer)
        ((IPacketContainer) container).receivePacket(packet.getData());
    } else if (packet.getChannel() == Tile) {
      final ByteArrayDataInput hdr = ByteStreams.newDataInput(packet.getHeader());
      final TileEntity t =
          packet.getPlayer().worldObj.getTileEntity(hdr.readInt(), hdr.readInt(), hdr.readInt());
      if (t instanceof APacketTile) {
        final APacketTile tb = (APacketTile) t;
        if (tb.getWorldObj().isRemote)
          tb.C_recievePacket(hdr.readByte(), packet.getData(), packet.getPlayer());
        else
          tb.S_recievePacket(hdr.readByte(), packet.getData(), packet.getPlayer());
      }
    } else if (packet.getChannel() == STATIC) {
      final ByteArrayDataInput hdr = ByteStreams.newDataInput(packet.getHeader());
      ReflectionHelper.invoke(registeredStaticHandlers.get(hdr.readUTF()), null, packet.getData());
    } else if (packet.getChannel() == KEY)
      YogpstopLib.proxy.setKeys(packet.getPlayer(), packet.getData()[0] << 24
          | packet.getData()[1] << 16 | packet.getData()[2] << 8 | packet.getData()[3]);
  }

  public static void sendPacketToServer(final YogpstopPacket p) {
    channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
        .set(OutboundTarget.TOSERVER);
    channels.get(Side.CLIENT).writeOutbound(p);
  }

  public static void sendPacketToAround(final YogpstopPacket p, final int d, final int x,
      final int y, final int z) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
        .set(OutboundTarget.ALLAROUNDPOINT);
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
        .set(new NetworkRegistry.TargetPoint(d, x, y, z, 256));
    channels.get(Side.SERVER).writeOutbound(p);
  }

  public static void sendPacketToDimension(final YogpstopPacket p, final int d) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
        .set(OutboundTarget.DIMENSION);
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new Integer(d));
    channels.get(Side.SERVER).writeOutbound(p);
  }

  public static void sendPacketToPlayer(final YogpstopPacket p, final EntityPlayer e) {
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
        .set(FMLOutboundHandler.OutboundTarget.PLAYER);
    channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(e);
    channels.get(Side.SERVER).writeOutbound(p);
  }

  private static void setNBTFromPacket(final YogpstopPacket p) {
    try {
      final NBTTagCompound cache =
          CompressedStreamTools.func_152457_a(p.getData(), NBTSizeTracker.field_152451_a);
      final TileEntity te =
          p.getPlayer().worldObj.getTileEntity(cache.getInteger("x"), cache.getInteger("y"),
              cache.getInteger("z"));
      if (te != null)
        te.readFromNBT(cache);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sendPacketToServer(final APacketTile te, final byte id, final byte pos,
      final String data) {// BLjava.lang.String;
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final DataOutputStream dos = new DataOutputStream(bos);
    try {
      dos.writeByte(pos);
      dos.writeUTF(data);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    sendPacketToServer(new YogpstopPacket(bos.toByteArray(), te, id));
  }

  public static void sendNowPacket(final APacketTile te, final byte data) {
    sendPacketToAround(new YogpstopPacket(new byte[] {data}, te, StC_NOW),
        te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
  }

  public static void sendPacketToServer(final APacketTile te, final byte id) {
    sendPacketToServer(new YogpstopPacket(new byte[0], te, id));
  }

  public static void sendPacketToAround(final APacketTile te, final byte id) {
    sendPacketToAround(new YogpstopPacket(new byte[0], te, id),
        te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
  }
}
