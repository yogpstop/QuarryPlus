package com.yogpc.mc_lib;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.yogpc.ip.ItemArmorElectric;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "YogpstopLib", name = "Yogpstop Library", version = "{version}",
    dependencies = "after:IC2")
public class YogpstopLib {

  @SidedProxy(clientSide = "com.yogpc.mc_lib.ProxyClient",
      serverSide = "com.yogpc.mc_lib.ProxyCommon")
  public static ProxyCommon proxy;

  private Block workbench;

  @Mod.EventHandler
  public void init(final FMLInitializationEvent event) {
    PacketHandler.channels =
        NetworkRegistry.INSTANCE.newChannel("YogpstopLib", new YogpstopPacketCodec(),
            new PacketHandler());
    new ItemArmorElectric();// TODO IC2Plus
    try {
      this.workbench = new BlockWorkbench();
    } catch (final Exception e) {
      e.printStackTrace();
    }
    GameRegistry.addRecipe(new ItemStack(this.workbench, 1), new Object[] {"III", "GDG", "RRR",
        Character.valueOf('D'), Blocks.diamond_block, Character.valueOf('R'), Items.redstone,
        Character.valueOf('I'), Blocks.iron_block, Character.valueOf('G'), Blocks.gold_block});
    GameRegistry.registerBlock(this.workbench, "WorkbenchPlus");
    GameRegistry.registerTileEntity(TileWorkbench.class, "WorkbenchPlus");
  }
}
