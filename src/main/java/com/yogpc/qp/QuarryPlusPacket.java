package com.yogpc.qp;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public final class QuarryPlusPacket {
	private byte channel;
	private byte[] data;
	private EntityPlayer ep;

	public QuarryPlusPacket() {}

	public QuarryPlusPacket(byte c, byte[] d) {
		this.channel = c;
		this.data = d;
	}

	public EntityPlayer getPlayer() {
		return this.ep;
	}

	public byte getChannel() {
		return this.channel;
	}

	public byte[] getData() {
		return this.data;
	}

	public void readData(ByteBuf d, ChannelHandlerContext ctx) {
		INetHandler h = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
		if (h instanceof NetHandlerPlayServer) {
			this.ep = ((NetHandlerPlayServer) h).playerEntity;
		} else {
			try {
				this.ep = Minecraft.getMinecraft().thePlayer;
			} catch (Exception e) {
				this.ep = null;
			}
		}
		this.channel = d.readByte();
		this.data = d.array();
	}

	public void writeData(ByteBuf d) {
		d.writeByte(this.channel);
		d.writeBytes(this.data);
	}
}
