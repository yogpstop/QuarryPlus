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

package com.yogpc.qp;

import java.lang.reflect.Field;

import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.ReflectionHelper;
import com.yogpc.qp.client.RenderEntityLaser;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;
import buildcraft.BuildCraftTransport;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.transport.PipeWire;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "{version}", dependencies = "required-after:YogpstopLib;required-after:BuildCraft|Builders;required-after:BuildCraft|Core;required-after:BuildCraft|Energy;required-after:BuildCraft|Factory;required-after:BuildCraft|Silicon;required-after:BuildCraft|Transport")
public class QuarryPlus {
	public static final CreativeTabs ct = new CreativeTabQuarryPlus();
	@SidedProxy(clientSide = "com.yogpc.qp.client.ClientProxy", serverSide = "com.yogpc.qp.CommonProxy")
	public static CommonProxy proxy;
	@Mod.Instance("QuarryPlus")
	public static QuarryPlus instance;
	public static final int refineryRenderID = RenderingRegistry.getNextAvailableRenderId();
	public static final int laserRenderID = RenderingRegistry.getNextAvailableRenderId();
	public static final int markerRenderID = RenderingRegistry.getNextAvailableRenderId();
	public static final int frameRenderID = RenderingRegistry.getNextAvailableRenderId();
	public static Block blockQuarry, blockMarker, blockMover, blockMiningWell, blockPump, blockInfMJSrc, blockRefinery, blockPlacer, blockBreaker, blockLaser,
			blockPlainPipe, blockFrame;
	public static Item itemTool;
	public static int RecipeDifficulty;
	public static final Field redstoneChipsetF = ReflectionHelper.getField(BuildCraftSilicon.class, "redstoneChipset");
	public static final int guiIdInfMJSrc = 1;
	public static final int guiIdMover = 2;
	public static final int guiIdFList = 3;
	public static final int guiIdSList = 4;
	public static final int guiIdPlacer = 5;
	public static final int guiIdPump = 6;// reserved from 6 to 11

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		TileMarker.Link[] la = TileMarker.linkList.toArray(new TileMarker.Link[TileMarker.linkList.size()]);
		for (TileMarker.Link l : la)
			if (l.w == event.world) l.removeConnection(false);
		TileMarker.Laser[] lb = TileMarker.laserList.toArray(new TileMarker.Laser[TileMarker.laserList.size()]);
		for (TileMarker.Laser l : lb)
			if (l.w == event.world) l.destructor();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre evt) {
		if (evt.map.getTextureType() == 0) {
			TextureMap map = evt.map;
			RenderEntityLaser.icons = new IIcon[4];
			RenderEntityLaser.icons[EntityLaser.DRILL] = map.registerIcon("yogpstop_qp:blockDrillTexture");
			RenderEntityLaser.icons[EntityLaser.DRILL_HEAD] = map.registerIcon("yogpstop_qp:blockDrillHeadTexture");
			RenderEntityLaser.icons[EntityLaser.RED_LASER] = map.registerIcon("yogpstop_qp:blockRedLaser");
			RenderEntityLaser.icons[EntityLaser.BLUE_LASER] = map.registerIcon("yogpstop_qp:blockBlueLaser");
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		try {
			cfg.load();
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
			blockQuarry = (new BlockQuarry());
			blockMarker = (new BlockMarker());
			blockMover = (new BlockMover());
			blockMiningWell = (new BlockMiningWell());
			blockPump = (new BlockPump());
			blockInfMJSrc = (new BlockInfMJSrc());
			blockRefinery = (new BlockRefinery());
			blockPlacer = (new BlockPlacer());
			blockBreaker = (new BlockBreaker());
			blockLaser = (new BlockLaser());
			blockPlainPipe = (new BlockPlainPipe());
			blockFrame = (new BlockFrame());
			itemTool = (new ItemTool());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingHandler());
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static final void addAssemblyRecipe(int a, ItemStack b, ItemStack... c) {
		BuildcraftRecipes.assemblyTable.addRecipe(a, b, c);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerItem(itemTool, "qpTool");
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
		GameRegistry.registerBlock(blockPlainPipe, "PlainPipePlus");
		GameRegistry.registerBlock(blockFrame, "FramePlus");
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
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftBuilders.markerBlock, Character.valueOf('X'), Items.redstone });
			GameRegistry.addRecipe(new ItemStack(blockQuarry, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.quarryBlock, Character.valueOf('X'), Items.redstone });
			GameRegistry.addRecipe(new ItemStack(blockMover, 1), new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.autoWorkbenchBlock,
					Character.valueOf('X'), Items.redstone });
			GameRegistry.addRecipe(new ItemStack(blockMiningWell, 1), new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.miningWellBlock,
					Character.valueOf('X'), Items.redstone });
			GameRegistry.addRecipe(new ItemStack(blockPump, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.pumpBlock, Character.valueOf('X'), Items.redstone });
			GameRegistry.addRecipe(new ItemStack(blockRefinery, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftFactory.refineryBlock, Character.valueOf('X'), Items.redstone });
			GameRegistry.addRecipe(new ItemStack(itemTool, 1, 0),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftCore.wrenchItem, Character.valueOf('X'), Items.sign });
			GameRegistry.addRecipe(new ItemStack(itemTool, 1, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftCore.wrenchItem, Character.valueOf('X'), Items.paper });
			GameRegistry.addRecipe(new ItemStack(itemTool, 1, 2),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftCore.wrenchItem, Character.valueOf('X'), Items.bucket });
			GameRegistry.addRecipe(new ItemStack(blockBreaker, 1), new Object[] { "X", "Y", Character.valueOf('Y'), Blocks.dispenser, Character.valueOf('X'),
					Items.iron_pickaxe });
			GameRegistry.addRecipe(new ItemStack(blockPlacer, 1), new Object[] { "X", "Y", Character.valueOf('Y'), Blocks.dispenser, Character.valueOf('X'),
					Items.redstone });
			GameRegistry.addRecipe(new ItemStack(blockLaser, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), BuildCraftSilicon.laserBlock, Character.valueOf('X'), Items.redstone });
			break;
		case 1:
			addAssemblyRecipe(160000, new ItemStack(blockQuarry, 1), new ItemStack(BuildCraftFactory.quarryBlock, 1), new ItemStack(redstoneChipset, 1, 3),
					new ItemStack(BuildCraftTransport.pipeWire, 8, PipeWire.YELLOW.ordinal()));
			addAssemblyRecipe(40000, new ItemStack(blockMover, 1), new ItemStack(Blocks.enchanting_table, 1), new ItemStack(
					BuildCraftFactory.autoWorkbenchBlock, 1), new ItemStack(Blocks.anvil, 1), new ItemStack(BuildCraftSilicon.laserBlock, 2), new ItemStack(
					BuildCraftSilicon.assemblyTableBlock, 1));
			addAssemblyRecipe(10000, new ItemStack(blockMarker, 1), new ItemStack(BuildCraftBuilders.markerBlock, 1), new ItemStack(redstoneChipset, 1, 2));
			addAssemblyRecipe(160000, new ItemStack(blockPump, 1), new ItemStack(BuildCraftFactory.pumpBlock, 1),
					new ItemStack(BuildCraftFactory.tankBlock, 32), new ItemStack(BuildCraftTransport.pipeFluidsGold, 2));
			addAssemblyRecipe(80000, new ItemStack(blockRefinery, 1), new ItemStack(BuildCraftFactory.refineryBlock, 1), new ItemStack(redstoneChipset, 1, 3),
					new ItemStack(BuildCraftFactory.hopperBlock, 2));
			addAssemblyRecipe(20000, new ItemStack(itemTool, 1, 0), new ItemStack(BuildCraftCore.wrenchItem, 2), new ItemStack(BuildCraftCore.ironGearItem, 2),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1));
			addAssemblyRecipe(40000, new ItemStack(itemTool, 1, 1), new ItemStack(BuildCraftCore.wrenchItem, 2), new ItemStack(Items.writable_book, 1),
					new ItemStack(Items.book, 16));
			addAssemblyRecipe(160000, new ItemStack(itemTool, 1, 2), new ItemStack(BuildCraftCore.wrenchItem, 2), new ItemStack(Items.bucket, 2),
					new ItemStack(Items.water_bucket, 1), new ItemStack(Items.lava_bucket, 1));
			addAssemblyRecipe(80000, new ItemStack(blockMiningWell, 1), new ItemStack(BuildCraftFactory.miningWellBlock, 1), new ItemStack(redstoneChipset, 1,
					3), new ItemStack(BuildCraftTransport.pipeWire, 8, PipeWire.YELLOW.ordinal()));
			addAssemblyRecipe(40000, new ItemStack(blockBreaker, 1), new ItemStack(Blocks.dispenser, 1), new ItemStack(Items.diamond_pickaxe, 1),
					new ItemStack(BuildCraftEnergy.engineBlock, 1, 1));
			addAssemblyRecipe(80000, new ItemStack(blockPlacer, 1), new ItemStack(Blocks.dispenser, 1), new ItemStack(BuildCraftBuilders.fillerBlock, 1),
					new ItemStack(BuildCraftEnergy.engineBlock, 1, 1));
			addAssemblyRecipe(160000, new ItemStack(blockLaser, 1), new ItemStack(BuildCraftSilicon.laserBlock, 1), new ItemStack(
					BuildCraftTransport.pipePowerGold, 32), new ItemStack(Blocks.glass, 16));
			break;
		case 3:
			addAssemblyRecipe(800000, new ItemStack(blockQuarry, 1), new ItemStack(BuildCraftFactory.quarryBlock, 2), new ItemStack(redstoneChipset, 8, 3),
					new ItemStack(BuildCraftTransport.pipeWire, 16, PipeWire.YELLOW.ordinal()), new ItemStack(redstoneChipset, 2, 4), new ItemStack(
							Blocks.chest, 32));
			addAssemblyRecipe(640000, new ItemStack(blockMover, 1), new ItemStack(Blocks.enchanting_table, 1), new ItemStack(
					BuildCraftSilicon.assemblyTableBlock, 1, 1), new ItemStack(Blocks.anvil, 2), new ItemStack(BuildCraftSilicon.laserBlock, 4), new ItemStack(
					BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 4, 3), new ItemStack(BuildCraftCore.diamondGearItem, 2));
			addAssemblyRecipe(160000, new ItemStack(blockMarker, 1), new ItemStack(BuildCraftBuilders.markerBlock, 1), new ItemStack(redstoneChipset, 4, 2),
					new ItemStack(redstoneChipset, 4, 3));
			addAssemblyRecipe(640000, new ItemStack(blockPump, 1), new ItemStack(BuildCraftFactory.pumpBlock, 2),
					new ItemStack(BuildCraftFactory.tankBlock, 64), new ItemStack(BuildCraftTransport.pipeFluidsGold, 32), new ItemStack(
							BuildCraftFactory.quarryBlock, 1));
			addAssemblyRecipe(640000, new ItemStack(blockRefinery, 1), new ItemStack(BuildCraftFactory.refineryBlock, 2), new ItemStack(
					BuildCraftFactory.tankBlock, 16), new ItemStack(Blocks.anvil, 2), new ItemStack(BuildCraftSilicon.laserBlock, 4), new ItemStack(
					BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 2, 3), new ItemStack(BuildCraftCore.diamondGearItem, 2));
			addAssemblyRecipe(160000, new ItemStack(itemTool, 1, 0), new ItemStack(BuildCraftCore.wrenchItem, 4), new ItemStack(BuildCraftCore.diamondGearItem,
					2), new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(BuildCraftBuilders.fillerBlock, 1), new ItemStack(
					BuildCraftBuilders.markerBlock, 4));
			addAssemblyRecipe(320000, new ItemStack(itemTool, 1, 1), new ItemStack(BuildCraftCore.wrenchItem, 4), new ItemStack(Items.writable_book, 1),
					new ItemStack(Items.book, 64), new ItemStack(redstoneChipset, 8, 3));
			addAssemblyRecipe(640000, new ItemStack(itemTool, 1, 2), new ItemStack(BuildCraftCore.wrenchItem, 4), new ItemStack(Items.bucket, 6),
					new ItemStack(Items.water_bucket, 1), new ItemStack(Items.lava_bucket, 1), new ItemStack(BuildCraftEnergy.bucketOil, 1), new ItemStack(
							BuildCraftEnergy.bucketFuel, 1), new ItemStack(BuildCraftEnergy.engineBlock, 1, 2));
			addAssemblyRecipe(500000, new ItemStack(blockMiningWell, 1), new ItemStack(BuildCraftFactory.miningWellBlock, 2), new ItemStack(redstoneChipset, 8,
					3), new ItemStack(BuildCraftTransport.pipeWire, 16, PipeWire.YELLOW.ordinal()), new ItemStack(redstoneChipset, 1, 4), new ItemStack(
					Blocks.chest, 16));
			addAssemblyRecipe(640000, new ItemStack(blockBreaker, 1), new ItemStack(Blocks.dispenser, 2), new ItemStack(Blocks.diamond_block, 2),
					new ItemStack(Items.redstone, 64), new ItemStack(blockQuarry, 1), new ItemStack(blockMiningWell, 1), new ItemStack(
							BuildCraftEnergy.engineBlock, 16, 2));
			addAssemblyRecipe(1280000, new ItemStack(blockPlacer, 1), new ItemStack(Blocks.dispenser, 2), new ItemStack(Blocks.diamond_block, 2),
					new ItemStack(Items.redstone, 64), new ItemStack(BuildCraftBuilders.fillerBlock, 2), new ItemStack(Blocks.gold_block, 2), new ItemStack(
							BuildCraftEnergy.engineBlock, 16, 2));
			addAssemblyRecipe(7654321, new ItemStack(blockLaser, 1), new ItemStack(BuildCraftSilicon.laserBlock, 16), new ItemStack(Blocks.glass, 64),
					new ItemStack(BuildCraftTransport.pipePowerGold, 64), new ItemStack(Items.glowstone_dust, 64), new ItemStack(Blocks.obsidian, 16));
			break;
		default:
			addAssemblyRecipe(320000, new ItemStack(blockQuarry, 1), new ItemStack(BuildCraftFactory.quarryBlock, 1), new ItemStack(redstoneChipset, 2, 3),
					new ItemStack(BuildCraftTransport.pipeWire, 16, PipeWire.YELLOW.ordinal()), new ItemStack(redstoneChipset, 1, 4), new ItemStack(
							Blocks.chest, 8));
			addAssemblyRecipe(320000, new ItemStack(blockMover, 1), new ItemStack(Blocks.enchanting_table, 1), new ItemStack(
					BuildCraftSilicon.assemblyTableBlock, 1, 1), new ItemStack(Blocks.anvil, 1), new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1),
					new ItemStack(redstoneChipset, 4, 3), new ItemStack(BuildCraftSilicon.laserBlock, 4));
			addAssemblyRecipe(20000, new ItemStack(blockMarker, 1), new ItemStack(BuildCraftBuilders.markerBlock, 1), new ItemStack(redstoneChipset, 4, 2),
					new ItemStack(BuildCraftCore.wrenchItem, 1));
			addAssemblyRecipe(320000, new ItemStack(blockPump, 1), new ItemStack(BuildCraftFactory.pumpBlock, 2),
					new ItemStack(BuildCraftFactory.tankBlock, 64), new ItemStack(BuildCraftTransport.pipeFluidsGold, 8), new ItemStack(
							BuildCraftTransport.pipeFluidsStone, 32));
			addAssemblyRecipe(640000, new ItemStack(blockRefinery, 1), new ItemStack(BuildCraftFactory.refineryBlock, 1), new ItemStack(
					BuildCraftFactory.tankBlock, 8), new ItemStack(Blocks.anvil, 1), new ItemStack(BuildCraftSilicon.laserBlock, 2), new ItemStack(
					BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(redstoneChipset, 1, 3), new ItemStack(BuildCraftCore.diamondGearItem, 1));
			addAssemblyRecipe(80000, new ItemStack(itemTool, 1, 0), new ItemStack(BuildCraftCore.wrenchItem, 2), new ItemStack(BuildCraftCore.goldGearItem, 2),
					new ItemStack(BuildCraftSilicon.assemblyTableBlock, 1), new ItemStack(BuildCraftBuilders.markerBlock, 2));
			addAssemblyRecipe(160000, new ItemStack(itemTool, 1, 1), new ItemStack(BuildCraftCore.wrenchItem, 2), new ItemStack(Items.writable_book, 1),
					new ItemStack(Items.book, 32), new ItemStack(redstoneChipset, 2, 3));
			addAssemblyRecipe(320000, new ItemStack(itemTool, 1, 2), new ItemStack(BuildCraftCore.wrenchItem, 2), new ItemStack(Items.bucket, 6),
					new ItemStack(Items.water_bucket, 1), new ItemStack(Items.lava_bucket, 1), new ItemStack(BuildCraftEnergy.bucketOil, 1), new ItemStack(
							BuildCraftEnergy.bucketFuel, 1));
			addAssemblyRecipe(160000, new ItemStack(blockMiningWell, 1), new ItemStack(BuildCraftFactory.miningWellBlock, 1), new ItemStack(redstoneChipset, 2,
					3), new ItemStack(BuildCraftTransport.pipeWire, 16, PipeWire.YELLOW.ordinal()), new ItemStack(redstoneChipset, 1, 4), new ItemStack(
					Blocks.chest, 8));
			addAssemblyRecipe(320000, new ItemStack(blockBreaker, 1), new ItemStack(Blocks.dispenser, 2), new ItemStack(Blocks.diamond_block, 1),
					new ItemStack(Items.redstone, 32), new ItemStack(blockQuarry, 1), new ItemStack(BuildCraftEnergy.engineBlock, 1, 2));
			addAssemblyRecipe(640000, new ItemStack(blockPlacer, 1), new ItemStack(Blocks.dispenser, 2), new ItemStack(Blocks.diamond_block, 1), new ItemStack(
					Items.redstone, 32), new ItemStack(BuildCraftBuilders.fillerBlock, 1), new ItemStack(Blocks.gold_block, 2), new ItemStack(
					BuildCraftEnergy.engineBlock, 1, 2));
			addAssemblyRecipe(1280000, new ItemStack(blockLaser, 1), new ItemStack(BuildCraftSilicon.laserBlock, 4), new ItemStack(Blocks.glass, 64),
					new ItemStack(BuildCraftTransport.pipePowerGold, 64), new ItemStack(Items.glowstone_dust, 32));
		}
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		proxy.registerTextures();
		PacketHandler.registerStaticHandler(TileMarker.class);
	}

	public static class BlockData {
		public final String name;
		public final int meta;

		public BlockData(String n, int m) {
			this.name = n;
			this.meta = m;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof BlockData) return this.name.equals(((BlockData) o).name)
					&& (this.meta == ((BlockData) o).meta || this.meta == OreDictionary.WILDCARD_VALUE || ((BlockData) o).meta == OreDictionary.WILDCARD_VALUE);
			return false;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}
	}

	public static String getname(BlockData bd) {
		StringBuffer sb = new StringBuffer();
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
