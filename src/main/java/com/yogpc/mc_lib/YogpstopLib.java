package com.yogpc.mc_lib;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "YogpstopLib", name = "Yogpstop Library", version = "@VERSION@")
public class YogpstopLib {

	@SidedProxy(clientSide = "com.yogpc.mc_lib.ProxyClient", serverSide = "com.yogpc.mc_lib.ProxyCommon")
	public static ProxyCommon proxy;

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		PacketHandler.channels = NetworkRegistry.INSTANCE.newChannel("QuarryPlus", new YogpstopPacketCodec(), new PacketHandler());
	}
}
