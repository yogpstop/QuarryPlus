package com.yogpc.mc_lib;

import java.util.Arrays;
import java.util.List;

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

  public static Block workbench, controller;
  public static Item magicmirror, armor;
  public static final int guiIdWorkbench = 1;
  public static boolean disableController = false;
  public static List<String> spawnerBlacklist;

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    final Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
    try {
      cfg.load();
      disableController =
          cfg.get(Configuration.CATEGORY_GENERAL, "DisableSpawnerController", false).getBoolean(
              false);
      spawnerBlacklist =
          Arrays.asList(cfg.get(Configuration.CATEGORY_GENERAL, "SpawnerControllerEntityBlackList",
              new String[0]).getStringList());
    } finally {
      cfg.save();
    }
    workbench = new BlockWorkbench();
    if (!disableController)
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
    if (!disableController)
      GameRegistry.registerBlock(controller, "yogSC");
    GameRegistry.registerItem(magicmirror, "magicmirror");
    GameRegistry.registerItem(armor, "qpArmor");
    GameRegistry.registerTileEntity(TileWorkbench.class, "WorkbenchPlus");
    GameRegistry.addRecipe(new ItemStack(workbench, 1), new Object[] {"III", "GDG", "RRR",
        Character.valueOf('D'), Blocks.diamond_block, Character.valueOf('R'), Items.redstone,
        Character.valueOf('I'), Blocks.iron_block, Character.valueOf('G'), Blocks.gold_block});
    WorkbenchRecipe.addRecipe(new ItemStack(magicmirror, 1, 1), 32000, new ItemStack(
        Items.ender_eye, 400), new ItemStack(magicmirror, 50));
    WorkbenchRecipe.addRecipe(new ItemStack(magicmirror, 1, 2), 32000, new ItemStack(
        Items.ender_eye, 400), new ItemStack(magicmirror, 50), new ItemStack(Blocks.obsidian, 100),
        new ItemStack(Blocks.dirt, 200), new ItemStack(Blocks.planks, 200));
    WorkbenchRecipe.addRecipe(new ItemStack(armor), 128000, new ItemStack(Items.iron_ingot, 1600),
        new ItemStack(Items.coal, 3200), new ItemStack(Items.gold_ingot, 400), new ItemStack(
            Items.diamond, 360), new ItemStack(Items.nether_star, 1), new ItemStack(
            Items.ender_eye, 25), new ItemStack(Items.glowstone_dust, 100), new ItemStack(
            Items.dye, 100, 10));
    if (!disableController)
      WorkbenchRecipe.addRecipe(new ItemStack(controller), 1000000, new ItemStack(
          Items.nether_star, 50), new ItemStack(Items.rotten_flesh, 1000), new ItemStack(
          Items.arrow, 1000), new ItemStack(Items.bone, 1000),
          new ItemStack(Items.gunpowder, 1000), new ItemStack(Items.iron_ingot, 2000),
          new ItemStack(Items.gold_ingot, 1000), new ItemStack(Items.ghast_tear, 250),
          new ItemStack(Items.magma_cream, 500), new ItemStack(Items.blaze_rod, 700),
          new ItemStack(Items.carrot, 50), new ItemStack(Items.potato, 50));
    final WeightedRandomChestContent c =
        new WeightedRandomChestContent(new ItemStack(magicmirror), 1, 1, 9);
    ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(c);
    ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(c);
    NetworkRegistry.INSTANCE.registerGuiHandler(this, this);
    PacketHandler.registerStaticHandler(BlockController.class);
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
