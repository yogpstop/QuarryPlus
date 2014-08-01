package com.yogpc.mc_lib;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ProxyClient extends ProxyCommon {

	@Override
	public EntityPlayer getPacketPlayer(INetHandler inh) {
		if (inh instanceof NetHandlerPlayServer) return ((NetHandlerPlayServer) inh).playerEntity;
		return Minecraft.getMinecraft().thePlayer;
	}
}
