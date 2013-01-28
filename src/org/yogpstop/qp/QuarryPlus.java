package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.logging.Level;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
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

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "@VERSION@")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = {
		"QuarryPlusGUI", "QuarryPlusBQP" }, packetHandler = PacketHandler.class)
public class QuarryPlus {
	@SidedProxy(clientSide = "org.yogpstop.qp.client.ClientProxy", serverSide = "org.yogpstop.qp.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance("QuarryPlus")
	public static QuarryPlus instance;

	public static Block blockQuarry;
	public static Block blockMarker;
	public static Item itemBase;
	public static Block blockMover;

	public static ArrayList<String> silktouch;
	public static ArrayList<String> fortune;

	public static int RecipeDifficulty;

	public static int guiIdContainerQuarry = 1;
	public static int guiIdContainerMover = 2;

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
			itemBase = (new ItemBase(cfg.getItem("ModuleBase", 24005).getInt()));
			Property RD = cfg.get(Configuration.CATEGORY_GENERAL,
					"RecipeDifficulty", 2);
			RD.comment = "0:AsCheatRecipe,1:EasyRecipe,2:NormalRecipe(Default),3:HardRecipe,other:NormalRecipe";
			RecipeDifficulty = RD.getInt(2);
			silktouch = new ArrayList<String>();
			fortune = new ArrayList<String>();
			parseCommaIntArrayList(cfg.get(Configuration.CATEGORY_GENERAL,
					"SilktouchList", "").value, silktouch);
			parseCommaIntArrayList(
					cfg.get(Configuration.CATEGORY_GENERAL, "FortuneList", "").value,
					fortune);
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "Error Massage");
		} finally {
			cfg.save();
		}
		LanguageRegistry.instance().loadLocalization(
				"/org/yogpstop/qp/lang/en_US.lang", "en_US", false);
		LanguageRegistry.instance().loadLocalization(
				"/org/yogpstop/qp/lang/ja_JP.lang", "ja_JP", false);
	}

	public void parseCommaIntArrayList(String source, ArrayList<String> output) {
		source.trim();
		if (source != "") {
			String[] cache = source.split(",");
			for (int i = 0; i < cache.length; i++) {
				String[] cache2 = cache[i].split(":");
				output.add(Integer.valueOf(cache2[0]).toString()
						+ ":"
						+ (cache2.length == 1 ? "0" : Integer
								.valueOf(cache2[1]).toString()));
			}
		}
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
			GameRegistry.addRecipe(new ItemStack(blockQuarry, 1), new Object[] {
					"X", "Y", Character.valueOf('Y'),
					BuildCraftFactory.quarryBlock, Character.valueOf('X'),
					Item.redstone });
			GameRegistry.addRecipe(
					new ItemStack(blockMover, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'),
							BuildCraftFactory.autoWorkbenchBlock,
							Character.valueOf('X'), Item.redstone });
			GameRegistry
					.addRecipe(new ItemStack(itemBase, 1, 0), new Object[] {
							"X", "Y", Character.valueOf('Y'), Block.stone,
							Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(itemBase, 1, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), itemBase,
							Character.valueOf('X'), Block.stone });
			GameRegistry.addRecipe(new ItemStack(itemBase, 1, 2),
					new Object[] { "X", "Y", Character.valueOf('Y'), itemBase,
							Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(itemBase, 1, 3),
					new Object[] { "X", "Y", Character.valueOf('Y'), itemBase,
							Character.valueOf('X'), Item.ingotIron });
			break;
		case 1:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", Character.valueOf('Y'),
					BuildCraftBuilders.markerBlock, Character.valueOf('X'),
					Item.ingotGold });
			GameRegistry.addRecipe(new ItemStack(blockQuarry, 1), new Object[] {
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
			GameRegistry.addRecipe(new ItemStack(itemBase, 1),
					new Object[] { "XDX", "YYY", Character.valueOf('Y'),
							Block.stone, Character.valueOf('X'), Item.redstone,
							Character.valueOf('D'), Item.diamond });
			break;
		case 3:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", "Z", Character.valueOf('X'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
					Character.valueOf('Y'), BuildCraftBuilders.markerBlock,
					Character.valueOf('Z'), BuildCraftCore.diamondGearItem });
			GameRegistry.addRecipe(new ItemStack(blockQuarry, 1), new Object[] {
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
			GameRegistry.addRecipe(new ItemStack(itemBase, 1), new Object[] {
					"SBS", "GBG", "SBS", Character.valueOf('S'), Block.stone,
					Character.valueOf('G'), BuildCraftCore.diamondGearItem,
					Character.valueOf('B'), Block.blockDiamond });
			break;
		default:
			GameRegistry.addRecipe(new ItemStack(blockMarker, 1), new Object[] {
					"X", "Y", Character.valueOf('X'),
					new ItemStack(BuildCraftSilicon.redstoneChipset, 1, 2),
					Character.valueOf('Y'), BuildCraftBuilders.markerBlock });
			GameRegistry.addRecipe(new ItemStack(blockQuarry, 1), new Object[] {
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
			GameRegistry.addRecipe(new ItemStack(itemBase, 1), new Object[] {
					"SGS", "SBS", "SGS", Character.valueOf('S'), Block.stone,
					Character.valueOf('G'), BuildCraftCore.diamondGearItem,
					Character.valueOf('B'), Block.blockDiamond });
		}
		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		proxy.registerTextures();
	}
}