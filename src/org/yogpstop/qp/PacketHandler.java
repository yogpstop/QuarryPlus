package org.yogpstop.qp;

import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet250CustomPayload;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.network.IPacketHandler;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager network,
			Packet250CustomPayload packet, Player player) {

		if (packet.channel.equals("QuarryPlus")) {
			ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);

			Container container = ((EntityPlayerMP) player).openContainer;
			if (container != null) {
				if (container instanceof ContainerMover) {
					((ContainerMover) container).readPacketData(data);
				}
				if (container instanceof ContainerQuarry) {
					((ContainerQuarry) container).readPacketData(data);
				}
			}
		}
	}

	public static Packet getPacket(ContainerMover containerMover) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);

		containerMover.writePacketData(dos);

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "QuarryPlus";
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
		packet.channel = "QuarryPlus";
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		packet.isChunkDataPacket = true;

		return packet;
	}

}