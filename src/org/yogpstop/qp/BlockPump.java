package org.yogpstop.qp;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

import java.util.ArrayList;

import buildcraft.api.tools.IToolWrench;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockPump extends BlockContainer {

	private Icon textureTop, textureBottom, textureSide, texW, texC;

	public BlockPump(int i) {
		super(i, Material.iron);
		setHardness(5F);
		setCreativeTab(tabBuildCraft);
		setUnlocalizedName("PumpPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TilePump();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int i, int j) {
		switch (i) {
		case 0:
			return this.textureBottom;
		case 1:
			return this.textureTop;
		default:
			return this.textureSide;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess ba, int x, int y, int z, int side) {
		TileEntity tile = ba.getBlockTileEntity(x, y, z);
		if (tile instanceof TilePump && side == 1) {
			if (((TilePump) tile).G_working()) return this.texW;
			if (((TilePump) tile).C_connected()) return this.texC;
		}
		return super.getBlockTexture(ba, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		this.textureTop = par1IconRegister.registerIcon("yogpstop/quarryplus:pump_top");
		this.textureBottom = par1IconRegister.registerIcon("yogpstop/quarryplus:pump_bottom");
		this.textureSide = par1IconRegister.registerIcon("yogpstop/quarryplus:pump_side");
		this.texW = par1IconRegister.registerIcon("yogpstop/quarryplus:pump_top_w");
		this.texC = par1IconRegister.registerIcon("yogpstop/quarryplus:pump_top_c");
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		this.drop.clear();
		if (world.isRemote) return;
		TilePump tp = (TilePump) world.getBlockTileEntity(x, y, z);
		int count = quantityDropped(meta, 0, world.rand);
		int id1 = idDropped(meta, world.rand, 0);
		if (id1 > 0) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(id1, 1, damageDropped(meta));
				tp.S_setEnchantment(is);
				this.drop.add(is);
			}
		}
		super.breakBlock(world, x, y, z, id, meta);
	}

	@Override
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {
		return this.drop;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el, ItemStack stack) {
		super.onBlockPlacedBy(w, x, y, z, el, stack);
		((TilePump) w.getBlockTileEntity(x, y, z)).G_init(stack.getEnchantmentTagList());
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, int bid) {
		((TilePump) w.getBlockTileEntity(x, y, z)).G_reinit();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			((TilePump) world.getBlockTileEntity(x, y, z)).changeRange(ep);
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		if (equipped instanceof ItemTool) {
			if (ep.getCurrentEquippedItem().getItemDamage() == 0) {
				if (world.isRemote) return true;
				ep.sendChatToPlayer(StatCollector.translateToLocal("chat.pumplist"));
				for (String s : ((TilePump) world.getBlockTileEntity(x, y, z)).C_getNames())
					ep.sendChatToPlayer(s);
				ep.sendChatToPlayer(StatCollector.translateToLocal("chat.plusenchant"));
				for (String s : ((TilePump) world.getBlockTileEntity(x, y, z)).C_getEnchantments())
					ep.sendChatToPlayer(s);
				return true;
			}
			if (ep.getCurrentEquippedItem().getItemDamage() == 2) {
				if (world.isRemote) return true;
				ep.sendChatToPlayer(StatCollector.translateToLocalFormatted("chat.pumptoggle", ((TilePump) world.getBlockTileEntity(x, y, z)).incl(side),
						TilePump.fdToString(ForgeDirection.getOrientation(side))));
				return true;
			}
		}
		return false;
	}
}
