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

package org.yogpstop.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.network.IPacketHandler;

public class PacketHandler implements IPacketHandler {
	public static final String Tile = "QPTile";
	public static final String NBT = "QPTENBT";
	public static final String BTN = "QPGUIBUTTON";
	public static final String Marker = "QPMarker";

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
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals(NBT)) {
			setNBTFromPacket(packet, (EntityPlayer) player);
		} else if (packet.channel.equals(BTN)) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			Container container = ((EntityPlayer) player).openContainer;
			if (container instanceof ContainerMover) ((ContainerMover) container).moveEnchant(data.readByte());
		} else if (packet.channel.equals(Tile)) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			TileEntity t = ((EntityPlayer) player).worldObj.getBlockTileEntity(data.readInt(), data.readInt(), data.readInt());
			if (t instanceof APacketTile) {
				APacketTile tb = (APacketTile) t;
				if (tb.worldObj.isRemote) tb.C_recievePacket(data.readByte(), data, (EntityPlayer) player);
				else tb.S_recievePacket(data.readByte(), data, (EntityPlayer) player);
			}
		} else if (packet.channel.equals(Marker)) {
			TileMarker.recieveLinkPacket(packet.data);
		}
	}

	static Packet getPacketFromNBT(TileEntity te) {
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = NBT;
		pkt.isChunkDataPacket = true;
		try {
			NBTTagCompound nbttc = new NBTTagCompound();
			te.writeToNBT(nbttc);
			byte[] bytes = CompressedStreamTools.compress(nbttc);
			pkt.data = bytes;
			pkt.length = bytes.length;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pkt;
	}

	private static void setNBTFromPacket(Packet250CustomPayload p, EntityPlayer ep) {
		try {
			NBTTagCompound cache;
			cache = CompressedStreamTools.decompress(p.data);
			TileEntity te = (ep).worldObj.getBlockTileEntity(cache.getInteger("x"), cache.getInteger("y"), cache.getInteger("z"));
			if (te != null) te.readFromNBT(cache);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Packet250CustomPayload composeTilePacket(ByteArrayOutputStream bos) {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = Tile;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		return packet;
	}

	public static void sendPacketToServer(APacketTile te, byte id, long data) {// J
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
			dos.writeLong(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToServer(composeTilePacket(bos));
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
		PacketDispatcher.sendPacketToServer(composeTilePacket(bos));
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
		PacketDispatcher.sendPacketToAllAround(te.xCoord, te.yCoord, te.zCoord, 256, te.worldObj.provider.dimensionId, composeTilePacket(bos));
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
		PacketDispatcher.sendPacketToServer(composeTilePacket(bos));
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
		PacketDispatcher.sendPacketToAllAround(te.xCoord, te.yCoord, te.zCoord, 256, te.worldObj.provider.dimensionId, composeTilePacket(bos));
	}
}
