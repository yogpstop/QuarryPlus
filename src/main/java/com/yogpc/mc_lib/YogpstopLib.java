package com.yogpc.mc_lib;

import com.yogpc.ip.ItemArmorElectric;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "YogpstopLib", name = "Yogpstop Library", version = "{version}",
    dependencies = "after:IC2")
public class YogpstopLib {

  @SidedProxy(clientSide = "com.yogpc.mc_lib.ProxyClient",
      serverSide = "com.yogpc.mc_lib.ProxyCommon")
  public static ProxyCommon proxy;

  @Mod.EventHandler
  public void init(final FMLInitializationEvent event) {
    PacketHandler.channels =
        NetworkRegistry.INSTANCE.newChannel("YogpstopLib", new YogpstopPacketCodec(),
            new PacketHandler());
    new ItemArmorElectric();// TODO IC2Plus
  }
}
