/*
 * Copyright (C) 2012,2013 yogpstop
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the
 * GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.yogpstop.qp;

import java.lang.reflect.Field;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.recipes.AssemblyRecipe;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Property;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "@VERSION@", dependencies = "required-after:BuildCraft|Builders;required-after:BuildCraft|Core;required-after:BuildCraft|Energy;required-after:BuildCraft|Factory;required-after:BuildCraft|Silicon;required-after:BuildCraft|Transport")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = { PacketHandler.BTN, PacketHandler.NBT, PacketHandler.Tile, PacketHandler.Marker }, packetHandler = PacketHandler.class)
public class QuarryPlus {
	@SidedProxy(clientSide = "org.yogpstop.qp.client.ClientProxy", serverSide = "org.yogpstop.qp.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance("QuarryPlus")
	public static QuarryPlus instance;

	public static final int refineryRenderID = RenderingRegistry.getNextAvailableRenderId();

	public static Block blockQuarry, blockMarker, blockMover, blockMiningWell, blockPump, blockInfMJSrc, blockRefinery, blockPlacer, blockBreaker, blockLaser;
	public static Item itemTool;

	public static int RecipeDifficulty;

	public static Field redstoneChipsetF = null;
	static {
		try {
			redstoneChipsetF = BuildCraftSilicon.class.getField("redstoneChipset");
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	public static final int guiIdInfMJSrc = 1;
	public static final int guiIdMover = 2;
	public static final int guiIdFList = 3;
	public static final int guiIdSList = 4;
	public static final int guiIdPlacer = 5;
	public static final int guiIdPump = 6;// reserved from 6 to 11

	@ForgeSubscribe
	public void onWorldUnload(WorldEvent.Unload event) {
		TileMarker.Link[] la = TileMarker.linkList.toArray(new TileMarker.Link[TileMarker.linkList.size()]);
		for (TileMarker.Link l : la)
			if (l.w == event.world) l.removeConnection(false);
		TileMarker.Laser[] lb = TileMarker.laserList.toArray(new TileMarker.Laser[TileMarker.laserList.size()]);
		for (TileMarker.Laser l : lb)
			if (l.w == event.world) l.destructor();
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		int[] bid = null;
		int iid = Integer.MIN_VALUE;
		try {
			cfg.load();
			bid = new int[] { cfg.getBlock("Quarry", 1970).getInt(), cfg.getBlock("Marker", 1971).getInt(), cfg.getBlock("EnchantMover", 1972).getInt(),
					cfg.getBlock("MiningWell", 1973).getInt(), cfg.getBlock("Pump", 1974).getInt(), cfg.getBlock("InfMJSrc", 1975).getInt(),
					cfg.getBlock("Refinery", 1976).getInt(), cfg.getBlock("Placer", 1977).getInt(), cfg.getBlock("Breaker", 1978).getInt(),
					cfg.getBlock("Laser", 1979).getInt() };
			iid = cfg.getItem("Tools", 18463).getInt();
			Property RD = cfg.get(Configuration.CATEGORY_GENERAL, "RecipeDifficulty", 2);
			RD.comment = "0:AsCheatRecipe,1:EasyRecipe,2:NormalRecipe(Default),3:HardRecipe,other:NormalRecipe";
			RecipeDifficulty = RD.getInt(2);
			PowerManager.loadConfiguration(cfg);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cfg.save();
		}
		try {
			blockQuarry = (new BlockQuarry(bid[0]));
			blockMarker = (new BlockMarker(bid[1]));
			blockMover = (new BlockMover(bid[2]));
			blockMiningWell = (new BlockMiningWell(bid[3]));
			blockPump = (new BlockPump(bid[4]));
			blockInfMJSrc = (new BlockInfMJSrc(bid[5]));
			blockRefinery = (new BlockRefinery(bid[6]));
			blockPlacer = (new BlockPlacer(bid[7]));
			blockBreaker = (new BlockBreaker(bid[8]));
			blockLaser = (new BlockLaser(bid[9]));
			itemTool = (new ItemTool(iid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		LanguageRegistry.instance().loadLocalization("/lang/yogpstop/quarryplus/en_US.lang", "en_US", false);
		LanguageRegistry.instance().loadLocalization("/lang/yogpstop/quarryplus/ja_JP.lang", "ja_JP", false);
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingHandler());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerBlock(blockQuarry, ItemBlockQuarry.class, "QuarryPlus");
		GameRegistry.registerBlock(blockMarker, "MarkerPlus");
		GameRegistry.registerBlock(blockMover, "EnchantMover");
		GameRegistry.registerBlock(blockMiningWell, ItemBlockQuarry.class, "MiningWellPlus");
		GameRegistry.registerBlock(blockPump, ItemBlockPump.class, "PumpPlus");
		GameRegistry.registerBlock(blockInfMJSrc, "InfMJSrc");
		GameRegistry.registerBlock(blockRefinery, ItemBlockRefinery.class, "RefineryPlus");
		GameRegistry.registerBlock(blockPlacer, "PlacerPlus");
		GameRegistry.registerBlock(blockBreaker, ItemBlockBreaker.class, "BreakerPlus");
		GameRegistry.registerBlock(blockLaser, ItemBlockQuarry.class, "LaserPlus");

		GameRegistry.registerTileEntity(TileQuarry.class, "QuarryPlus");
		GameRegistry.registerTileEntity(TileMarker.class, "MarkerPlus");
		GameRegistry.registerTileEntity(TileMiningWell.class, "MiningWellPlus");
		GameRegistry.registerTileEntity(TilePump.class, "PumpPlus");
		GameRegistry.registerTileEntity(TileInfMJSrc.class, "InfMJSrc");
		GameRegistry.registerTileEntity(TileRefinery.class, "RefineryPlus");
		GameRegistry.registerTileEntity(TilePlacer.class, "PlacerPlus");
		GameRegistry.registerTileEntity(TileBreaker.class, "BreakerPlus");
		GameRegistry.registerTileEntity(TileLaser.class, "LaserPlus");

		Item redstoneChipset = null;
		try {
			redstoneChipset = (Item) redstoneChipsetF.get(null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		switch (RecipeDifficulty) {
		case 0:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftBuilders.markerBlock, Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(blockQuarry, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.quarryBlock, Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(blockMover, 1), new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.autoWorkbenchBlock,
					Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(blockMiningWell, 1), new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.miningWellBlock,
					Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(blockPump, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.pumpBlock, Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(blockRefinery, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.refineryBlock, Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(itemTool, 1, 0),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftCore.wrenchItem, Character.valueOf('X'), Item.sign });
			GameRegistry.addRecipe(new ItemStack(itemTool, 1, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftCore.wrenchItem, Character.valueOf('X'), Item.paper });
			GameRegistry.addRecipe(new ItemStack(itemTool, 1, 2),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftCore.wrenchItem, Character.valueOf('X'), Item.bucketEmpty });
			GameRegistry.addRecipe(new ItemStack(blockBreaker, 1), new Object[] { "X", "Y", Character.valueOf('Y'), Block.dispenser, Character.valueOf('X'),
					Item.pickaxeIron });
			GameRegistry.addRecipe(new ItemStack(blockPlacer, 1), new Object[] { "X", "Y", Character.valueOf('Y'), Block.dispenser, Character.valueOf('X'),
					Item.redstone });
			GameRegistry.addRecipe(new ItemStack(blockLaser, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftSilicon.laserBlock, Character.valueOf('X'), Item.redstone });
			break;
		case 1:
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.quarryBlock, 1),
					new ItemStack(redstoneChipset, 1, 3), new ItemStack(BuildCraftTransport.yellowPipeWire, 8) }, 160000, new ItemStack(blockQuarry, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.enchantmentTable, 1),
					new ItemStack(BuildCraftFactory.autoWorkbenchBlock, 1), new ItemStack(Block.anvil, 1), new ItemStack(BuildCraftSilicon.laserBlock, 2),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1) }, 40000, new ItemStack(blockMover, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftBuilders.markerBlock, 1),
					new ItemStack(redstoneChipset, 1, 2) }, 10000, new ItemStack(blockMarker, 1)));
			AssemblyRecipe.assemblyRecipes
					.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.pumpBlock, 1), new ItemStack(BuildCraftFactory.tankBlock, 32),
							new ItemStack(BuildCraftTransport.pipeFluidsGold, 2) }, 160000, new ItemStack(blockPump, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.refineryBlock, 1),
					new ItemStack(redstoneChipset, 1, 3), new ItemStack(BuildCraftFactory.hopperBlock, 2) }, 80000, new ItemStack(blockRefinery, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 2),
					new ItemStack(BuildCraftCore.ironGearItem, 2), new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1) }, 20000, new ItemStack(itemTool, 1,
					0)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 2),
					new ItemStack(Item.writableBook, 1), new ItemStack(Item.book, 16) }, 40000, new ItemStack(itemTool, 1, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 2),
					new ItemStack(Item.bucketEmpty, 2), new ItemStack(Item.bucketWater, 1), new ItemStack(Item.bucketLava, 1) }, 160000, new ItemStack(
					itemTool, 1, 2)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.miningWellBlock, 1),
					new ItemStack(redstoneChipset, 1, 3), new ItemStack(BuildCraftTransport.yellowPipeWire, 8) }, 80000, new ItemStack(blockMiningWell, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.dispenser, 1), new ItemStack(Item.pickaxeDiamond, 1),
					new ItemStack(BuildCraftEnergy.engineBlock, 1, 1) }, 40000, new ItemStack(blockBreaker, 1)));
			AssemblyRecipe.assemblyRecipes
					.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.dispenser, 1), new ItemStack(BuildCraftBuilders.fillerBlock, 1),
							new ItemStack(BuildCraftEnergy.engineBlock, 1, 1) }, 80000, new ItemStack(blockPlacer, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftSilicon.laserBlock, 1),
					new ItemStack(BuildCraftTransport.pipePowerGold, 32), new ItemStack(Block.glass, 16) }, 160000, new ItemStack(blockLaser, 1)));
			break;
		case 3:
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.quarryBlock, 2),
					new ItemStack(redstoneChipset, 8, 3), new ItemStack(BuildCraftTransport.yellowPipeWire, 16), new ItemStack(redstoneChipset, 2, 4),
					new ItemStack(Block.chest, 32) }, 800000, new ItemStack(blockQuarry, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.enchantmentTable, 1),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1, 1), new ItemStack(Block.anvil, 2), new ItemStack(BuildCraftSilicon.laserBlock, 4),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 4, 3),
					new ItemStack(BuildCraftCore.diamondGearItem, 2) }, 640000, new ItemStack(blockMover, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftBuilders.markerBlock, 1),
					new ItemStack(redstoneChipset, 4, 2), new ItemStack(redstoneChipset, 4, 3) }, 160000, new ItemStack(blockMarker, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.pumpBlock, 2),
					new ItemStack(BuildCraftFactory.tankBlock, 64), new ItemStack(BuildCraftTransport.pipeFluidsGold, 32),
					new ItemStack(BuildCraftFactory.quarryBlock, 1) }, 640000, new ItemStack(blockPump, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.refineryBlock, 2),
					new ItemStack(BuildCraftFactory.tankBlock, 16), new ItemStack(Block.anvil, 2), new ItemStack(BuildCraftSilicon.laserBlock, 4),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 2, 3),
					new ItemStack(BuildCraftCore.diamondGearItem, 2) }, 640000, new ItemStack(blockRefinery, 1)));
			AssemblyRecipe.assemblyRecipes
					.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 4), new ItemStack(BuildCraftCore.diamondGearItem, 2),
							new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(BuildCraftBuilders.fillerBlock, 1),
							new ItemStack(BuildCraftBuilders.markerBlock, 4) }, 160000, new ItemStack(itemTool, 1, 0)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 4),
					new ItemStack(Item.writableBook, 1), new ItemStack(Item.book, 64), new ItemStack(redstoneChipset, 8, 3) }, 320000, new ItemStack(itemTool,
					1, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 4),
					new ItemStack(Item.bucketEmpty, 6), new ItemStack(Item.bucketWater, 1), new ItemStack(Item.bucketLava, 1),
					new ItemStack(BuildCraftEnergy.bucketOil, 1), new ItemStack(BuildCraftEnergy.bucketFuel, 1),
					new ItemStack(BuildCraftEnergy.engineBlock, 1, 2) }, 640000, new ItemStack(itemTool, 1, 2)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.miningWellBlock, 2),
					new ItemStack(redstoneChipset, 8, 3), new ItemStack(BuildCraftTransport.yellowPipeWire, 16), new ItemStack(redstoneChipset, 1, 4),
					new ItemStack(Block.chest, 16) }, 500000, new ItemStack(blockMiningWell, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.dispenser, 2), new ItemStack(Block.blockDiamond, 2),
					new ItemStack(Item.redstone, 64), new ItemStack(blockQuarry, 1), new ItemStack(blockMiningWell, 1),
					new ItemStack(BuildCraftEnergy.engineBlock, 16, 2) }, 640000, new ItemStack(blockBreaker, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.dispenser, 2), new ItemStack(Block.blockDiamond, 2),
					new ItemStack(Item.redstone, 64), new ItemStack(BuildCraftBuilders.fillerBlock, 2), new ItemStack(Block.blockGold, 2),
					new ItemStack(BuildCraftEnergy.engineBlock, 16, 2) }, 1280000, new ItemStack(blockPlacer, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftSilicon.laserBlock, 16),
					new ItemStack(Block.glass, 64), new ItemStack(BuildCraftTransport.pipePowerGold, 64), new ItemStack(Item.glowstone, 64),
					new ItemStack(Block.obsidian, 16) }, 7654321, new ItemStack(blockLaser, 1)));
			break;
		default:
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.quarryBlock, 1),
					new ItemStack(redstoneChipset, 2, 3), new ItemStack(BuildCraftTransport.yellowPipeWire, 16), new ItemStack(redstoneChipset, 1, 4),
					new ItemStack(Block.chest, 8) }, 320000, new ItemStack(blockQuarry, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.enchantmentTable, 1),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1, 1), new ItemStack(Block.anvil, 1),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 4, 3),
					new ItemStack(BuildCraftSilicon.laserBlock, 4) }, 320000, new ItemStack(blockMover, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftBuilders.markerBlock, 1),
					new ItemStack(redstoneChipset, 4, 2), new ItemStack(BuildCraftCore.wrenchItem, 1) }, 20000, new ItemStack(blockMarker, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.pumpBlock, 2),
					new ItemStack(BuildCraftFactory.tankBlock, 64), new ItemStack(BuildCraftTransport.pipeFluidsGold, 8),
					new ItemStack(BuildCraftTransport.pipeFluidsStone, 32) }, 320000, new ItemStack(blockPump, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.refineryBlock, 1),
					new ItemStack(BuildCraftFactory.tankBlock, 8), new ItemStack(Block.anvil, 1), new ItemStack(BuildCraftSilicon.laserBlock, 2),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 1, 3),
					new ItemStack(BuildCraftCore.diamondGearItem, 1) }, 640000, new ItemStack(blockRefinery, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 2),
					new ItemStack(BuildCraftCore.goldGearItem, 2), new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1),
					new ItemStack(BuildCraftBuilders.markerBlock, 2) }, 80000, new ItemStack(itemTool, 1, 0)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 2),
					new ItemStack(Item.writableBook, 1), new ItemStack(Item.book, 32), new ItemStack(redstoneChipset, 2, 3) }, 160000, new ItemStack(itemTool,
					1, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftCore.wrenchItem, 2),
					new ItemStack(Item.bucketEmpty, 6), new ItemStack(Item.bucketWater, 1), new ItemStack(Item.bucketLava, 1),
					new ItemStack(BuildCraftEnergy.bucketOil, 1), new ItemStack(BuildCraftEnergy.bucketFuel, 1) }, 320000, new ItemStack(itemTool, 1, 2)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftFactory.miningWellBlock, 1),
					new ItemStack(redstoneChipset, 2, 3), new ItemStack(BuildCraftTransport.yellowPipeWire, 16), new ItemStack(redstoneChipset, 1, 4),
					new ItemStack(Block.chest, 8) }, 160000, new ItemStack(blockMiningWell, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.dispenser, 2), new ItemStack(Block.blockDiamond, 1),
					new ItemStack(Item.redstone, 32), new ItemStack(blockQuarry, 1), new ItemStack(BuildCraftEnergy.engineBlock, 1, 2) }, 320000,
					new ItemStack(blockBreaker, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(Block.dispenser, 2), new ItemStack(Block.blockDiamond, 1),
					new ItemStack(Item.redstone, 32), new ItemStack(BuildCraftBuilders.fillerBlock, 1), new ItemStack(Block.blockGold, 2),
					new ItemStack(BuildCraftEnergy.engineBlock, 1, 2) }, 640000, new ItemStack(blockPlacer, 1)));
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[] { new ItemStack(BuildCraftSilicon.laserBlock, 4),
					new ItemStack(Block.glass, 64), new ItemStack(BuildCraftTransport.pipePowerGold, 64), new ItemStack(Item.glowstone, 32) }, 1280000,
					new ItemStack(blockLaser, 1)));
		}
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
		proxy.registerTextures();
	}

	public static String getname(short blockid, int meta) {
		StringBuffer sb = new StringBuffer();
		sb.append(blockid);
		if (meta != 0) {
			sb.append(":");
			sb.append(meta);
		}
		sb.append("  ");
		ItemStack cache = new ItemStack(blockid, 1, meta);
		if (cache.getItem() == null) {
			sb.append(StatCollector.translateToLocal("tof.nullblock"));
		} else if (cache.getDisplayName() == null) {
			sb.append(StatCollector.translateToLocal("tof.nullname"));
		} else {
			sb.append(cache.getDisplayName());
		}
		return sb.toString();
	}

	public static String getname(long data) {
		return getname((short) (data % 0x1000), (int) (data >> 12));
	}

	public static long data(short id, int meta) {
		return id | (meta << 12);
	}

	public static CreativeTabs ct = null;
	static {
		final Class ctc = buildcraft.core.CreativeTabBuildCraft.class;
		try {
			ct = (CreativeTabs) ctc.getField("tabBuildCraft").get(null);
		} catch (Exception e) {}
		if (ct == null) {
			try {
				ct = (CreativeTabs) ctc.getMethod("get", new Class[] {}).invoke(ctc.getField("MACHINES").get(null), new Object[] {});
			} catch (Exception e) {}
		}
	}
}
