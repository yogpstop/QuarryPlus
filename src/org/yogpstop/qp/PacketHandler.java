package org.yogpstop.qp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

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
	public static final String Tile = "QuarryPlusTile";
	public static final String NBT = "QPTENBT";
	public static final String BTN = "QPGUIBUTTON";
	public static final String OGUI = "QPOpenGUI";

	public static final byte fortuneAdd = 1;
	public static final byte silktouchAdd = 2;
	public static final byte fortuneRemove = 3;
	public static final byte silktouchRemove = 4;
	public static final byte packetNow = 5;
	public static final byte packetHeadPos = 6;
	public static final byte fortuneTInc = 7;
	public static final byte silktouchTInc = 8;
	public static final byte packetFortuneList = 9;
	public static final byte packetSilktouchList = 10;
	public static final byte signal = 11;
	public static final byte link = 12;

	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals(NBT)) {
			setNBTFromPacket(packet, (EntityPlayer) player);
		} else if (packet.channel.equals(BTN)) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			Container container = ((EntityPlayer) player).openContainer;
			if (container != null) {
				if (container instanceof ContainerMover) {
					((ContainerMover) container).readPacketData(data);
				}
			}
		} else if (packet.channel.equals(Tile)) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			TileEntity t = ((EntityPlayer) player).worldObj.getBlockTileEntity(data.readInt(), data.readInt(), data.readInt());
			if (t instanceof APacketTile) {
				APacketTile tb = (APacketTile) t;
				if (tb.worldObj.isRemote) tb.C_recievePacket(data.readByte(), data);
				else tb.S_recievePacket(data.readByte(), data, (EntityPlayer) player);
			}
		} else if (packet.channel.equals(OGUI)) {
			openGuiFromPacket(ByteStreams.newDataInput(packet.data), (EntityPlayer) player);
		}
	}

	public static void sendOpenGUIPacket(int guiId, int x, int y, int z) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeByte(guiId);
			dos.writeInt(x);
			dos.writeInt(y);
			dos.writeInt(z);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = OGUI;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;
		PacketDispatcher.sendPacketToServer(packet);
	}

	private static void openGuiFromPacket(ByteArrayDataInput badi, EntityPlayer ep) {
		ep.openGui(QuarryPlus.instance, badi.readByte(), ep.worldObj, badi.readInt(), badi.readInt(), badi.readInt());
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

	private static Packet250CustomPayload composeTilePacket(ByteArrayOutputStream bos) {
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = Tile;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		return packet;
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

	public static void sendPacketToServer(APacketTile te, byte id, long data) {
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

	static void sendPacketToPlayer(APacketTile te, EntityPlayer ep, byte id, boolean value) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
			dos.writeBoolean(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToPlayer(composeTilePacket(bos), (Player) ep);
	}

	static void sendPacketToPlayer(APacketTile te, EntityPlayer ep, byte id, Collection<Long> value) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
			dos.writeInt(value.size());
			for (Long l : value)
				dos.writeLong(l);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToPlayer(composeTilePacket(bos), (Player) ep);
	}

	static void sendNowPacket(APacketTile te, byte data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(packetNow);
			dos.writeByte(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToAllPlayers(composeTilePacket(bos));
	}

	static void sendMarkerPacket(APacketTile te, byte id, boolean data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(id);
			dos.writeBoolean(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToAllPlayers(composeTilePacket(bos));
	}

	static void sendHeadPosPacket(APacketTile te, double x, double y, double z) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(packetHeadPos);
			dos.writeDouble(x);
			dos.writeDouble(y);
			dos.writeDouble(z);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToAllPlayers(composeTilePacket(bos));
	}

	static void sendLinkPacket(APacketTile te, TileMarker.Link l) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(link);
			dos.writeInt(l.xx);
			dos.writeInt(l.xn);
			dos.writeInt(l.yx);
			dos.writeInt(l.yn);
			dos.writeInt(l.zx);
			dos.writeInt(l.zn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToAllPlayers(composeTilePacket(bos));
	}

	static void sendLinkPacket(APacketTile te, TileMarker.Link l, EntityPlayer ep) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeInt(te.xCoord);
			dos.writeInt(te.yCoord);
			dos.writeInt(te.zCoord);
			dos.writeByte(link);
			dos.writeInt(l.xx);
			dos.writeInt(l.xn);
			dos.writeInt(l.yx);
			dos.writeInt(l.yn);
			dos.writeInt(l.zx);
			dos.writeInt(l.zn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		PacketDispatcher.sendPacketToPlayer(composeTilePacket(bos), (Player) ep);
	}
}
