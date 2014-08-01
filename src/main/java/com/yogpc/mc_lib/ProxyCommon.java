package com.yogpc.mc_lib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

public class ProxyCommon {

	public EntityPlayer getPacketPlayer(INetHandler inh) {
		if (inh instanceof NetHandlerPlayServer) return ((NetHandlerPlayServer) inh).playerEntity;
		return null;
	}
}
