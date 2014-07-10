package com.yogpc.qp;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.player.EntityPlayer;
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
		this.ep = QuarryPlus.proxy.getPacketPlayer(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
		this.channel = d.readByte();
		this.data = new byte[d.readableBytes()];
		d.readBytes(this.data);
	}

	public void writeData(ByteBuf d) {
		d.writeByte(this.channel);
		d.writeBytes(this.data);
	}
}
