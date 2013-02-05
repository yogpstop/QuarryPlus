package org.yogpstop.qp;

import java.util.logging.Level;

import org.yogpstop.qp.client.QuarryItemRenderer;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.Property;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.FMLLog;

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "@VERSION@", dependencies = "required-after:BuildCraft|Factory@[3.4.2,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = {
		"QuarryPlusGUI", "QuarryPlusBQP" }, packetHandler = PacketHandler.class)
public class QuarryPlus {
	@SidedProxy(clientSide = "org.yogpstop.qp.client.ClientProxy", serverSide = "org.yogpstop.qp.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance("QuarryPlus")
	public static QuarryPlus instance;

	public static Block blockQuarry;
	public static Block blockMarker;
	public static Block blockMover;

	public static Item itemQuarry;

	public static int RecipeDifficulty;

	public static final int guiIdContainerQuarry = 1;
	public static final int guiIdContainerMover = 2;

	@Mod.PreInit
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(
				event.getSuggestedConfigurationFile());
		try {
			cfg.load();
			blockQuarry = (new BlockQuarry(cfg.getBlock("Quarry", 4001)
					.getInt())).setBlockName("QuarryPlus");
			blockMarker = (new BlockMarker(cfg.getBlock("Marker", 4002)
					.getInt())).setBlockName("MarkerPlus");
			blockMover = (new BlockMover(cfg.getBlock("EnchantMover", 4003)
					.getInt())).setBlockName("EnchantMover");
			itemQuarry = (new ItemQuarry(cfg.getItem("ItemQuarry", 25001)
					.getInt())).setItemName("QuarryPlus");
			Property RD = cfg.get(Configuration.CATEGORY_GENERAL,
					"RecipeDifficulty", 2);
			RD.comment = "0:AsCheatRecipe,1:EasyRecipe,2:NormalRecipe(Default),3:HardRecipe,other:NormalRecipe";
			RecipeDifficulty = RD.getInt(2);
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "Error Massage");
		} finally {
			cfg.save();
		}
		LanguageRegistry.instance().loadLocalization(
				"/org/yogpstop/qp/lang/en_US.lang", "en_US", false);
		LanguageRegistry.instance().loadLocalization(
				"/org/yogpstop/qp/lang/ja_JP.lang", "ja_JP", false);
		ForgeChunkManager.setForcedChunkLoadingCallback(instance,
				new ChunkLoadingHandler());
	}

	@Mod.Init
	public void init(FMLInitializationEvent event) {

		GameRegistry.registerBlock(blockQuarry, "QuarryPlus");
		GameRegistry.registerBlock(blockMarker, "MarkerPlus");
		GameRegistry.registerBlock(blockMover, "EnchantMover");

		GameRegistry.registerTileEntity(TileQuarry.class, "QuarryPlus");
		GameRegistry.registerTileEntity(TileMarker.class, "MarkerPlus");

		switch (RecipeDifficulty) {
		case 0:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", Character.valueOf('Y'),
					BuildCraftBuilders.markerBlock, Character.valueOf('X'),
					Item.redstone });
			GameRegistry.addRecipe(new ItemStack(itemQuarry, 1), new Object[] {
					"X", "Y", Character.valueOf('Y'),
					BuildCraftFactory.quarryBlock, Character.valueOf('X'),
					Item.redstone });
			GameRegistry.addRecipe(
					new ItemStack(blockMover, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'),
							BuildCraftFactory.autoWorkbenchBlock,
							Character.valueOf('X'), Item.redstone });
			break;
		case 1:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", Character.valueOf('Y'),
					BuildCraftBuilders.markerBlock, Character.valueOf('X'),
					Item.ingotGold });
			GameRegistry.addRecipe(new ItemStack(itemQuarry, 1), new Object[] {
					" X ", "DYD", Character.valueOf('Y'),
					BuildCraftFactory.quarryBlock, Character.valueOf('X'),
					Block.anvil, Character.valueOf('D'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3) });
			GameRegistry.addRecipe(
					new ItemStack(blockMover, 1),
					new Object[] { "X", "Y", "Z", Character.valueOf('Z'),
							BuildCraftFactory.autoWorkbenchBlock,
							Character.valueOf('Y'), Block.anvil,
							Character.valueOf('X'), Block.enchantmentTable });
			break;
		case 3:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", "Z", Character.valueOf('X'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
					Character.valueOf('Y'), BuildCraftBuilders.markerBlock,
					Character.valueOf('Z'), BuildCraftCore.diamondGearItem });
			GameRegistry.addRecipe(new ItemStack(itemQuarry, 1), new Object[] {
					"GBG", "CQC", "WAW", Character.valueOf('G'),
					BuildCraftCore.diamondGearItem, Character.valueOf('B'),
					Block.blockDiamond, Character.valueOf('C'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3),
					Character.valueOf('Q'), BuildCraftFactory.quarryBlock,
					Character.valueOf('W'), BuildCraftCore.wrenchItem,
					Character.valueOf('A'),
					BuildCraftFactory.autoWorkbenchBlock });
			GameRegistry.addRecipe(new ItemStack(blockMover, 1), new Object[] {
					"DED", "GAG", "OOO", Character.valueOf('D'),
					Block.blockDiamond, Character.valueOf('E'),
					Block.enchantmentTable, Character.valueOf('O'),
					Block.obsidian, Character.valueOf('A'), Block.anvil,
					Character.valueOf('G'), BuildCraftCore.diamondGearItem });
			break;
		default:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", Character.valueOf('X'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
					Character.valueOf('Y'), BuildCraftBuilders.markerBlock });
			GameRegistry.addRecipe(new ItemStack(itemQuarry, 1), new Object[] {
					"GDG", "IQI", "WAW", Character.valueOf('G'),
					BuildCraftCore.goldGearItem, Character.valueOf('D'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 3),
					Character.valueOf('I'), BuildCraftCore.ironGearItem,
					Character.valueOf('Q'), BuildCraftFactory.quarryBlock,
					Character.valueOf('W'), BuildCraftCore.wrenchItem,
					Character.valueOf('A'),
					BuildCraftFactory.autoWorkbenchBlock });
			GameRegistry.addRecipe(new ItemStack(blockMover, 1), new Object[] {
					"DED", "OAO", "OOO", Character.valueOf('D'),
					BuildCraftCore.diamondGearItem, Character.valueOf('E'),
					Block.enchantmentTable, Character.valueOf('O'),
					Block.obsidian, Character.valueOf('A'), Block.anvil });
		}
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		MinecraftForgeClient.registerItemRenderer(itemQuarry.itemID,
				new QuarryItemRenderer());
		proxy.registerTextures();
	}
}