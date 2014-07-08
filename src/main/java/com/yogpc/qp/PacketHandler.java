/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumMap;

import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.qp.QuarryPlus.BlockData;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.FMLOutboundHandler.OutboundTarget;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;

@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<QuarryPlusPacket> {
	public static EnumMap<Side, FMLEmbeddedChannel> channels;
	public static final byte Tile = 0;
	public static final byte NBT = 1;
	public static final byte BTN = 2;
	public static final byte Marker = 3;

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
	protected void channelRead0(ChannelHandlerContext ctx, QuarryPlusPacket packet) throws Exception {
		if (packet.getChannel() == NBT) {
			setNBTFromPacket(packet);
		} else if (packet.getChannel() == BTN) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.getData());
			Container container = packet.getPlayer().openContainer;
			if (container instanceof ContainerMover) ((ContainerMover) container).moveEnchant(data.readByte());
		} else if (packet.getChannel() == Tile) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.getData());
			TileEntity t = packet.getPlayer().worldObj.getTileEntity(data.readInt(), data.readInt(), data.readInt());
			if (t instanceof APacketTile) {
				APacketTile tb = (APacketTile) t;
				if (tb.getWorldObj().isRemote) tb.C_recievePacket(data.readByte(), data, packet.getPlayer());
				else tb.S_recievePacket(data.readByte(), data, packet.getPlayer());
			}
		} else if (packet.getChannel() == Marker) {
			TileMarker.recieveLinkPacket(packet.getData());
		}
	}

	static QuarryPlusPacket getPacketFromNBT(TileEntity te) {
		try {
			NBTTagCompound nbttc = new NBTTagCompound();
			te.writeToNBT(nbttc);
			byte[] bytes = CompressedStreamTools.compress(nbttc);
			return new QuarryPlusPacket(NBT, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static void setNBTFromPacket(QuarryPlusPacket p) {
		try {
			NBTTagCompound cache;
			cache = CompressedStreamTools.decompress(p.getData());
			TileEntity te = p.getPlayer().worldObj.getTileEntity(cache.getInteger("x"), cache.getInteger("y"), cache.getInteger("z"));
			if (te != null) te.readFromNBT(cache);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendPacketToServer(APacketTile te, byte id, BlockData bd) {// Ljava.lang.String;I
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
			dos.writeUTF(bd.name);
			dos.writeInt(bd.meta);
		} catch (Exception e) {
			e.printStackTrace();
		}
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(new QuarryPlusPacket(Tile, bos.toByteArray()));
	}

	public static void sendPacketToServer(APacketTile te, byte id, byte pos, String data) {// BLjava.lang.String;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
			dos.writeByte(pos);
			dos.writeUTF(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(new QuarryPlusPacket(Tile, bos.toByteArray()));
	}

	static void sendNowPacket(APacketTile te, byte data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(StC_NOW);
			dos.writeByte(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
				.set(new NetworkRegistry.TargetPoint(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, 256));
		channels.get(Side.SERVER).writeOutbound(new QuarryPlusPacket(Tile, bos.toByteArray()));
	}

	public static void sendPacketToServer(APacketTile te, byte id) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeOutbound(new QuarryPlusPacket(Tile, bos.toByteArray()));
	}

	static void sendPacketToAround(APacketTile te, byte id) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
				.set(new NetworkRegistry.TargetPoint(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, 256));
		channels.get(Side.SERVER).writeOutbound(new QuarryPlusPacket(Tile, bos.toByteArray()));
	}
}
