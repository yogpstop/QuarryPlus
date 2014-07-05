package com.yogpc.qp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;

public class QuarryPlusPacketCodec extends FMLIndexedMessageToMessageCodec<QuarryPlusPacket> {

	public QuarryPlusPacketCodec() {
		this.addDiscriminator(0, QuarryPlusPacket.class);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, QuarryPlusPacket msg, ByteBuf target) throws Exception {
		msg.writeData(target);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, QuarryPlusPacket msg) {
		msg.readData(source, ctx);
	}

}
