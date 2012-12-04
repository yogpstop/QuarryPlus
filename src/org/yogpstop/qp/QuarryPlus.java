package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.BuildCraftSilicon;

import com.google.common.collect.Lists;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.OrderedLoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.Property;

import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.FMLLog;

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "@VERSION@")
@NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = "QuarryPlus", packetHandler = PacketHandler.class)
public class QuarryPlus {
	@SidedProxy(clientSide = "org.yogpstop.qp.client.ClientProxy", serverSide = "org.yogpstop.qp.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance("QuarryPlus")
	public static QuarryPlus instance;

	public static Block blockQuarry;
	public static Block blockMarker;
	public static Item itemSilktouch;
	public static Item itemFortune;
	public static Item itemEfficiency;
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
			itemSilktouch = (new ItemSilktouch(cfg.getItem("Silktouch", 24001)
					.getInt()));
			itemFortune = (new ItemFortune(cfg.getItem("Fortune", 24002)
					.getInt()));
			itemEfficiency = (new ItemEfficiency(cfg.getItem("Efficiency",
					24003).getInt()));
			itemBase = (new ItemBase(cfg.getItem("ModuleBase", 24005).getInt()));
			Property RD = cfg.get(Configuration.CATEGORY_GENERAL,
					"RecipeDifficulty", 2);
			RD.comment = "0:AsCheatRecipe,1:EasyRecipe,2:NormalRecipe(Default),3:HardRecipe,other:NormalRecipe";
			RecipeDifficulty = RD.getInt(2);
			silktouch = new ArrayList<String>();
			fortune = new ArrayList<String>();
			parseCommaIntArrayList(
					cfg.get(Configuration.CATEGORY_GENERAL, "SilktouchList","").value,
					silktouch);
			parseCommaIntArrayList(
					cfg.get(Configuration.CATEGORY_GENERAL, "FortuneList","").value,
					fortune);
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "Error Massage");
		} finally {
			cfg.save();
		}
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

		GameRegistry.registerBlock(blockQuarry);
		GameRegistry.registerBlock(blockMarker);
		GameRegistry.registerBlock(blockMover);

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
					.addRecipe(new ItemStack(itemBase, 1),
							new Object[] { "X", "Y", Character.valueOf('Y'),
									Block.stone, Character.valueOf('X'),
									Item.redstone });
			GameRegistry.addRecipe(new ItemStack(itemSilktouch, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), itemBase,
							Character.valueOf('X'), Block.stone });
			GameRegistry.addRecipe(new ItemStack(itemFortune, 1),
					new Object[] { "X", "Y", Character.valueOf('Y'), itemBase,
							Character.valueOf('X'), Item.redstone });
			GameRegistry.addRecipe(new ItemStack(itemEfficiency, 1),
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
		LanguageRegistry.addName(blockQuarry, "Quarry Plus");
		LanguageRegistry.addName(blockMarker, "Land Mark Plus");
		LanguageRegistry.addName(blockMover, "Enchant Mover");
		LanguageRegistry.addName(itemSilktouch, "Silktouch Module");
		LanguageRegistry.addName(itemEfficiency, "Efficiency Module");
		LanguageRegistry.addName(itemFortune, "Fortune Module");
		LanguageRegistry.addName(itemBase, "Module Base");

		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		proxy.registerTextures();
		proxy.initializeEntityRenders();
	}

	@Mod.PostInit
	public void postinit(FMLPostInitializationEvent evt) {
		ForgeChunkManager.setForcedChunkLoadingCallback(instance,
				new QuarryChunkloadCallback());
	}

	public class QuarryChunkloadCallback implements OrderedLoadingCallback {
		@Override
		public void ticketsLoaded(List<Ticket> tickets, World world) {
			for (Ticket ticket : tickets) {
				int quarryX = ticket.getModData().getInteger("quarryX");
				int quarryY = ticket.getModData().getInteger("quarryY");
				int quarryZ = ticket.getModData().getInteger("quarryZ");
				TileQuarry tq = (TileQuarry) world.getBlockTileEntity(quarryX,
						quarryY, quarryZ);
				tq.forceChunkLoading(ticket);

			}
		}

		@Override
		public List<Ticket> ticketsLoaded(List<Ticket> tickets, World world,
				int maxTicketCount) {
			List<Ticket> validTickets = Lists.newArrayList();
			for (Ticket ticket : tickets) {
				int quarryX = ticket.getModData().getInteger("quarryX");
				int quarryY = ticket.getModData().getInteger("quarryY");
				int quarryZ = ticket.getModData().getInteger("quarryZ");

				int blId = world.getBlockId(quarryX, quarryY, quarryZ);
				if (blId == blockQuarry.blockID) {
					validTickets.add(ticket);
				}
			}
			return validTickets;
		}

	}
}