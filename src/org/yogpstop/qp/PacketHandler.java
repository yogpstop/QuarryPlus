package org.yogpstop.qp;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.network.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager network,
			Packet250CustomPayload packet, Player player) {
		if (packet.channel.equals("QuarryPlusBQP")) {
			try {
				NBTTagCompound cache;
				cache = CompressedStreamTools.decompress(packet.data);
				TileQuarry tq = (TileQuarry) QuarryPlus.proxy.getClientWorld()
						.getBlockTileEntity(cache.getInteger("x"),
								cache.getInteger("y"), cache.getInteger("z"));
				if (tq != null)
					tq.readFromNBT(cache);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (packet.channel.equals("QuarryPlusGUI")) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
			Container container = ((EntityPlayer) player).openContainer;
			if (container != null) {
				if (container instanceof ContainerMover) {
					((ContainerMover) container).readPacketData(data);
				}
				if (container instanceof ContainerQuarry) {
					((ContainerQuarry) container).readPacketData(data,player);
				}
			}
		}
	}

	public static Packet getPacket(ContainerMover containerMover) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		containerMover.writePacketData(dos);

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusGUI";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		return packet;
	}

	public static Packet getPacket(ContainerQuarry containerQuarry) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		containerQuarry.writePacketData(dos);

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlusGUI";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		return packet;
	}

	public static Packet getPacket(TileQuarry tq) {
		try {
			NBTTagCompound tag = new NBTTagCompound();
			tq.writeToNBT(tag);
			byte[] bytes = CompressedStreamTools.compress(tag);
			Packet250CustomPayload pkt = new Packet250CustomPayload();
			pkt.channel = "QuarryPlusBQP";
			pkt.data = bytes;
			pkt.length = bytes.length;
			pkt.isChunkDataPacket = true;
			return pkt;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}