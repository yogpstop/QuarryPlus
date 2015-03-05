package com.yogpc.qp;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;

import com.yogpc.qp.block.BlockBreaker;
import com.yogpc.qp.block.BlockController;
import com.yogpc.qp.block.BlockFrame;
import com.yogpc.qp.block.BlockLaser;
import com.yogpc.qp.block.BlockMarker;
import com.yogpc.qp.block.BlockMiningWell;
import com.yogpc.qp.block.BlockMover;
import com.yogpc.qp.block.BlockPlacer;
import com.yogpc.qp.block.BlockPlainPipe;
import com.yogpc.qp.block.BlockPump;
import com.yogpc.qp.block.BlockQuarry;
import com.yogpc.qp.block.BlockRefinery;
import com.yogpc.qp.block.BlockWorkbench;
import com.yogpc.qp.entity.EntityLaser;
import com.yogpc.qp.gui.GuiHandler;
import com.yogpc.qp.item.ItemArmorElectric;
import com.yogpc.qp.item.ItemBlockBreaker;
import com.yogpc.qp.item.ItemBlockPump;
import com.yogpc.qp.item.ItemBlockQuarry;
import com.yogpc.qp.item.ItemBlockRefinery;
import com.yogpc.qp.item.ItemMirror;
import com.yogpc.qp.item.ItemTool;
import com.yogpc.qp.render.RenderEntityLaser;
import com.yogpc.qp.tile.TileBreaker;
import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileMarker;
import com.yogpc.qp.tile.TileMiningWell;
import com.yogpc.qp.tile.TilePlacer;
import com.yogpc.qp.tile.TilePump;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.tile.TileRefinery;
import com.yogpc.qp.tile.TileWorkbench;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class QuarryPlusI {
  public static final QuarryPlusI I = new QuarryPlusI();

  private QuarryPlusI() {}

  public static final CreativeTabs ct = new CreativeTabQuarryPlus();

  public static final int refineryRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static final int laserRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static final int markerRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static final int frameRenderID = RenderingRegistry.getNextAvailableRenderId();

  public static Block blockQuarry, blockMarker, blockMover, blockMiningWell, blockPump,
      blockRefinery, blockPlacer, blockBreaker, blockLaser, blockPlainPipe, blockFrame, workbench,
      controller;
  public static Item itemTool, magicmirror, armor;

  public static boolean disableController = false;
  public static List<String> spawnerBlacklist;

  public static final int guiIdWorkbench = 1;
  public static final int guiIdMover = 2;
  public static final int guiIdFList = 3;
  public static final int guiIdSList = 4;
  public static final int guiIdPlacer = 5;
  public static final int guiIdPump = 6;// reserved from 6 to 11

  @SubscribeEvent
  public void onWorldUnload(final WorldEvent.Unload event) {
    final TileMarker.Link[] la =
        TileMarker.linkList.toArray(new TileMarker.Link[TileMarker.linkList.size()]);
    for (final TileMarker.Link l : la)
      if (l.w == event.world)
        l.removeConnection(false);
    final TileMarker.Laser[] lb =
        TileMarker.laserList.toArray(new TileMarker.Laser[TileMarker.laserList.size()]);
    for (final TileMarker.Laser l : lb)
      if (l.w == event.world)
        l.destructor();
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void loadTextures(final TextureStitchEvent.Pre evt) {
    if (evt.map.getTextureType() == 0) {
      final TextureMap map = evt.map;
      RenderEntityLaser.icons = new IIcon[4];
      RenderEntityLaser.icons[EntityLaser.DRILL] =
          map.registerIcon("yogpstop_qp:blockDrillTexture");
      RenderEntityLaser.icons[EntityLaser.DRILL_HEAD] =
          map.registerIcon("yogpstop_qp:blockDrillHeadTexture");
      RenderEntityLaser.icons[EntityLaser.RED_LASER] =
          map.registerIcon("yogpstop_qp:blockRedLaser");
      RenderEntityLaser.icons[EntityLaser.BLUE_LASER] =
          map.registerIcon("yogpstop_qp:blockBlueLaser");
    }
  }

  public static void preInit(final FMLPreInitializationEvent event) {
    final Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
    try {
      cfg.load();
      disableController =
          cfg.get(Configuration.CATEGORY_GENERAL, "DisableSpawnerController", false).getBoolean(
              false);
      spawnerBlacklist =
          Arrays.asList(cfg.get(Configuration.CATEGORY_GENERAL, "SpawnerControllerEntityBlackList",
              new String[0]).getStringList());
      final Property RD = cfg.get(Configuration.CATEGORY_GENERAL, "RecipeDifficulty", 2);
      RD.comment = "Default is 2.0";
      WorkbenchRecipe.difficulty = RD.getDouble(2.0);
      PowerManager.loadConfiguration(cfg);
    } finally {
      cfg.save();
    }
    workbench = new BlockWorkbench();
    if (!disableController)
      controller = new BlockController();
    blockQuarry = new BlockQuarry();
    blockMarker = new BlockMarker();
    blockMover = new BlockMover();
    blockMiningWell = new BlockMiningWell();
    blockPump = new BlockPump();
    blockRefinery = new BlockRefinery();
    blockPlacer = new BlockPlacer();
    blockBreaker = new BlockBreaker();
    blockLaser = new BlockLaser();
    blockPlainPipe = new BlockPlainPipe();
    blockFrame = new BlockFrame();
    magicmirror = new ItemMirror();
    armor = new ItemArmorElectric();
    itemTool = new ItemTool();
    ForgeChunkManager.setForcedChunkLoadingCallback(QuarryPlus.I, new ChunkLoadingHandler());
    MinecraftForge.EVENT_BUS.register(I);
  }

  public static void init() {
    PacketHandler.channels =
        NetworkRegistry.INSTANCE.newChannel("QuarryPlus", new YogpstopPacketCodec(),
            new PacketHandler());
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
    PacketHandler.registerStaticHandler(BlockController.class);
    GameRegistry.registerItem(magicmirror, "magicmirror");
    GameRegistry.registerItem(armor, "qpArmor");
    GameRegistry.registerItem(itemTool, "qpTool");
    if (!disableController)
      GameRegistry.registerBlock(controller, "yogSC");
    GameRegistry.registerBlock(workbench, "WorkbenchPlus");
    GameRegistry.registerBlock(blockQuarry, ItemBlockQuarry.class, "QuarryPlus");
    GameRegistry.registerBlock(blockMarker, "MarkerPlus");
    GameRegistry.registerBlock(blockMover, "EnchantMover");
    GameRegistry.registerBlock(blockMiningWell, ItemBlockQuarry.class, "MiningWellPlus");
    GameRegistry.registerBlock(blockPump, ItemBlockPump.class, "PumpPlus");
    GameRegistry.registerBlock(blockRefinery, ItemBlockRefinery.class, "RefineryPlus");
    GameRegistry.registerBlock(blockPlacer, "PlacerPlus");
    GameRegistry.registerBlock(blockBreaker, ItemBlockBreaker.class, "BreakerPlus");
    GameRegistry.registerBlock(blockLaser, ItemBlockQuarry.class, "LaserPlus");
    GameRegistry.registerBlock(blockPlainPipe, "PlainPipePlus");
    GameRegistry.registerBlock(blockFrame, "FramePlus");
    GameRegistry.registerTileEntity(TileWorkbench.class, "WorkbenchPlus");
    GameRegistry.registerTileEntity(TileQuarry.class, "QuarryPlus");
    GameRegistry.registerTileEntity(TileMarker.class, "MarkerPlus");
    GameRegistry.registerTileEntity(TileMiningWell.class, "MiningWellPlus");
    GameRegistry.registerTileEntity(TilePump.class, "PumpPlus");
    GameRegistry.registerTileEntity(TileRefinery.class, "RefineryPlus");
    GameRegistry.registerTileEntity(TilePlacer.class, "PlacerPlus");
    GameRegistry.registerTileEntity(TileBreaker.class, "BreakerPlus");
    GameRegistry.registerTileEntity(TileLaser.class, "LaserPlus");
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
    WorkbenchRecipe.addRecipe(new ItemStack(blockMarker), 20000,
        new ItemStack(Items.redstone, 300), new ItemStack(Items.dye, 300, 4), new ItemStack(
            Items.gold_ingot, 175), new ItemStack(Items.iron_ingot, 150), new ItemStack(
            Items.glowstone_dust, 50), new ItemStack(Items.ender_pearl, 10));
    WorkbenchRecipe.addRecipe(new ItemStack(blockQuarry), 320000,
        new ItemStack(Items.diamond, 800), new ItemStack(Items.gold_ingot, 800), new ItemStack(
            Items.iron_ingot, 1600), new ItemStack(Items.redstone, 400), new ItemStack(
            Items.ender_pearl, 50), new ItemStack(Items.nether_star, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(blockMover), 320000, new ItemStack(Blocks.obsidian,
        1600), new ItemStack(Items.diamond, 800), new ItemStack(Blocks.anvil, 50), new ItemStack(
        Items.redstone, 1200), new ItemStack(Items.gold_ingot, 200), new ItemStack(
        Items.iron_ingot, 200), new ItemStack(Items.nether_star, 1), new ItemStack(
        Items.ender_pearl, 25));
    WorkbenchRecipe.addRecipe(new ItemStack(blockMiningWell), 160000, new ItemStack(
        Items.iron_ingot, 800), new ItemStack(Items.redstone, 400), new ItemStack(Items.diamond,
        100), new ItemStack(Items.ender_pearl, 50), new ItemStack(Items.nether_star, 1),
        new ItemStack(Items.gold_ingot, 25));
    WorkbenchRecipe.addRecipe(new ItemStack(blockPump), 320000, new ItemStack(Items.iron_ingot,
        1200), new ItemStack(Items.redstone, 1600), new ItemStack(Blocks.glass, 12800),
        new ItemStack(Blocks.cactus, 2000), new ItemStack(Items.gold_ingot, 400), new ItemStack(
            Items.nether_star, 1), new ItemStack(Items.ender_pearl, 10));
    WorkbenchRecipe.addRecipe(new ItemStack(blockRefinery), 640000, new ItemStack(Items.diamond,
        900), new ItemStack(Items.gold_ingot, 600), new ItemStack(Items.iron_ingot, 600),
        new ItemStack(Blocks.glass, 3200), new ItemStack(Items.redstone, 800), new ItemStack(
            Blocks.anvil, 50), new ItemStack(Blocks.obsidian, 600), new ItemStack(
            Items.nether_star, 1), new ItemStack(Items.ender_pearl, 20));
    WorkbenchRecipe.addRecipe(new ItemStack(itemTool, 1, 0), 80000, new ItemStack(Items.gold_ingot,
        400), new ItemStack(Items.iron_ingot, 600), new ItemStack(Blocks.obsidian, 100),
        new ItemStack(Items.diamond, 100), new ItemStack(Items.redstone, 400), new ItemStack(
            Items.dye, 100, 4), new ItemStack(Items.ender_pearl, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(itemTool, 1, 1), 160000, new ItemStack(
        Items.iron_ingot, 400), new ItemStack(Items.book, 1600), new ItemStack(Items.feather, 50),
        new ItemStack(Items.dye, 400), new ItemStack(Items.diamond, 100), new ItemStack(
            Items.redstone, 100), new ItemStack(Items.ender_pearl, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(itemTool, 1, 2), 320000, new ItemStack(
        Items.iron_ingot, 1600), new ItemStack(Items.lava_bucket, 60), new ItemStack(
        Items.water_bucket, 60), new ItemStack(Items.ender_pearl, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(blockBreaker), 320000, new ItemStack(Items.redstone,
        1600), new ItemStack(Items.diamond, 600), new ItemStack(Items.gold_ingot, 800),
        new ItemStack(Items.iron_ingot, 1600), new ItemStack(Items.ender_pearl, 50));
    WorkbenchRecipe.addRecipe(new ItemStack(blockPlacer), 320000, new ItemStack(Items.redstone,
        1600), new ItemStack(Items.diamond, 600), new ItemStack(Items.gold_ingot, 1600),
        new ItemStack(Items.iron_ingot, 800), new ItemStack(Items.ender_pearl, 50));
    WorkbenchRecipe.addRecipe(new ItemStack(blockLaser), 640000, new ItemStack(Items.diamond, 400),
        new ItemStack(Items.redstone, 4800), new ItemStack(Blocks.obsidian, 800), new ItemStack(
            Blocks.glass, 3600), new ItemStack(Items.glowstone_dust, 1600), new ItemStack(
            Items.gold_ingot, 800), new ItemStack(Items.ender_pearl, 5));
    NetworkRegistry.INSTANCE.registerGuiHandler(QuarryPlus.I, new GuiHandler());
    QuarryPlus.proxy.registerTextures();
    PacketHandler.registerStaticHandler(TileMarker.class);
  }

  public static class BlockData {
    public final String name;
    public final int meta;

    public BlockData(final String n, final int m) {
      this.name = n;
      this.meta = m;
    }

    @Override
    public boolean equals(final Object o) {
      if (o instanceof BlockData)
        return this.name.equals(((BlockData) o).name)
            && (this.meta == ((BlockData) o).meta || this.meta == OreDictionary.WILDCARD_VALUE || ((BlockData) o).meta == OreDictionary.WILDCARD_VALUE);
      return false;
    }

    @Override
    public int hashCode() {
      return this.name.hashCode();
    }
  }

  @SideOnly(Side.CLIENT)
  public static String getLocalizedName(final BlockData bd) {
    final StringBuffer sb = new StringBuffer();
    sb.append(bd.name);
    if (bd.meta != OreDictionary.WILDCARD_VALUE) {
      sb.append(":");
      sb.append(bd.meta);
    }
    sb.append("  ");
    sb.append(GameData.getBlockRegistry().getObject(bd.name).getLocalizedName());
    return sb.toString();
  }
}
