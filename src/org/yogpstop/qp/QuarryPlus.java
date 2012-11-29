package org.yogpstop.qp;

import java.util.List;
import java.util.logging.Level;

import com.google.common.collect.Lists;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.OrderedLoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

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

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "1.0.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
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
	public static Item itemDrillModule;

	public static int guiIdContainerQuarry = 1;

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
			itemSilktouch = (new ItemSilktouch(cfg.getItem("Silktouch", 24001)
					.getInt()));
			itemFortune = (new ItemFortune(cfg.getItem("Fortune", 24002)
					.getInt()));
			itemEfficiency = (new ItemEfficiency(cfg.getItem("Efficiency",
					24003).getInt()));
			itemDrillModule = (new ItemDrillModule(cfg.getItem("DrillModule",
					24004).getInt()));

		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "Error Massage");
		} finally {
			cfg.save();
		}
	}

	@Mod.Init
	public void init(FMLInitializationEvent event) {

		GameRegistry.registerBlock(blockQuarry);
		GameRegistry.registerBlock(blockMarker);

		GameRegistry.registerTileEntity(TileQuarry.class, "QuarryPlus");
		GameRegistry.registerTileEntity(TileMarker.class, "MarkerPlus");

		/*
		 * GameRegistry.addRecipe(new ItemStack(blockQuarry, 1), new Object[] {
		 * " X ", " X ", Character.valueOf('X'), Block.dirt });
		 */

		LanguageRegistry.addName(blockQuarry, "Quarry Plus");
		LanguageRegistry.addName(blockMarker, "Land Mark Plus");
		LanguageRegistry.addName(itemSilktouch, "Silktouch Module");
		LanguageRegistry.addName(itemEfficiency, "Efficiency Module");
		LanguageRegistry.addName(itemFortune, "Fortune Module");
		LanguageRegistry.addName(itemDrillModule, "Drill Module");

		NetworkRegistry.instance().registerGuiHandler(this, proxy);

		proxy.registerTextures();
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