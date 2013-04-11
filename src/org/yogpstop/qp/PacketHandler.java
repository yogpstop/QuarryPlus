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

    @Override
    public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player) {
        if (packet.channel.equals("QPTENBT")) {
            setNBTFromPacket(packet, (EntityPlayer) player);
        } else if (packet.channel.equals("QuarryPlusGUIBtn")) {
            ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
            Container container = ((EntityPlayer) player).openContainer;
            if (container != null) {
                if (container instanceof ContainerMover) {
                    ((ContainerMover) container).readPacketData(data);
                }
            }
        } else if (packet.channel.equals("QuarryPlusTQ")) {
            ByteArrayDataInput data = ByteStreams.newDataInput(packet.data);
            TileQuarry tq = (TileQuarry) ((EntityPlayer) player).worldObj.getBlockTileEntity(data.readInt(), data.readInt(), data.readInt());
            if (tq != null)
                tq.recievePacket(data, (EntityPlayer) player);
        } else if (packet.channel.equals("QPOpenGUI")) {
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
        packet.channel = "QPOpenGUI";
        packet.data = bos.toByteArray();
        packet.length = bos.size();
        packet.isChunkDataPacket = true;
        PacketDispatcher.sendPacketToServer(packet);
    }

    private static void openGuiFromPacket(ByteArrayDataInput badi, EntityPlayer ep) {
        ep.openGui(QuarryPlus.instance, badi.readByte(), ep.worldObj, badi.readInt(), badi.readInt(), badi.readInt());
    }

    public static Packet getPacketFromNBT(TileEntity te) {
        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "QPTENBT";
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
            if (te != null)
                te.readFromNBT(cache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}