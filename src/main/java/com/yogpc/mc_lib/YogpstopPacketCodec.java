package com.yogpc.mc_lib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;

public class YogpstopPacketCodec extends FMLIndexedMessageToMessageCodec<YogpstopPacket> {

	public YogpstopPacketCodec() {
		this.addDiscriminator(0, YogpstopPacket.class);
	}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, YogpstopPacket msg, ByteBuf target) throws Exception {
		msg.writeData(target);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, YogpstopPacket msg) {
		msg.readData(source, ctx);
	}

}
