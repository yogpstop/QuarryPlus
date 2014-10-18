package com.yogpc.mc_lib;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;

import com.yogpc.ip.BlockController;
import com.yogpc.ip.ItemArmorElectric;
import com.yogpc.ip.ItemMirror;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "YogpstopLib", name = "Yogpstop Library", version = "{version}",
    dependencies = "after:IC2;after:BuildCraft|Core;after:CoFHCore")
public class YogpstopLib implements IGuiHandler {

  @SidedProxy(clientSide = "com.yogpc.mc_lib.ProxyClient",
      serverSide = "com.yogpc.mc_lib.ProxyCommon")
  public static ProxyCommon proxy;
  @Mod.Instance("YogpstopLib")
  public static YogpstopLib instance;

  private static Block workbench, controller;
  private static Item magicmirror, armor;
  public static final int guiIdWorkbench = 1;

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    final Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
    try {
      cfg.load();
    } finally {
      cfg.save();
    }
    workbench = new BlockWorkbench();
    controller = new BlockController();
    magicmirror = new ItemMirror();
    armor = new ItemArmorElectric();
  }

  @Mod.EventHandler
  public void init(final FMLInitializationEvent event) {
    PacketHandler.channels =
        NetworkRegistry.INSTANCE.newChannel("YogpstopLib", new YogpstopPacketCodec(),
            new PacketHandler());
    GameRegistry.registerBlock(workbench, "WorkbenchPlus");
    GameRegistry.registerBlock(controller, "yogSC");
    GameRegistry.registerItem(magicmirror, "magicmirror");
    GameRegistry.registerItem(armor, "qpArmor");
    GameRegistry.registerTileEntity(TileWorkbench.class, "WorkbenchPlus");
    GameRegistry.addRecipe(new ItemStack(workbench, 1), new Object[] {"III", "GDG", "RRR",
        Character.valueOf('D'), Blocks.diamond_block, Character.valueOf('R'), Items.redstone,
        Character.valueOf('I'), Blocks.iron_block, Character.valueOf('G'), Blocks.gold_block});
    GameRegistry.addRecipe(new ItemStack(magicmirror, 1, 1), "###", "#X#", "###",
        Character.valueOf('#'), new ItemStack(Items.ender_eye, 1, 0), Character.valueOf('X'),
        new ItemStack(magicmirror, 1, 0));
    final WeightedRandomChestContent c =
        new WeightedRandomChestContent(new ItemStack(magicmirror, 1, 0), 1, 1, 9);
    ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(c);
    NetworkRegistry.INSTANCE.registerGuiHandler(this, this);
  }

  @Override
  public Object getServerGuiElement(final int ID, final EntityPlayer p, final World w, final int x,
      final int y, final int z) {
    switch (ID) {
      case guiIdWorkbench:
        return new ContainerWorkbench(p.inventory, (TileWorkbench) w.getTileEntity(x, y, z));
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(final int ID, final EntityPlayer p, final World w, final int x,
      final int y, final int z) {
    switch (ID) {
      case guiIdWorkbench:
        return new GuiWorkbench(p.inventory, (TileWorkbench) w.getTileEntity(x, y, z));
    }
    return null;
  }
}
