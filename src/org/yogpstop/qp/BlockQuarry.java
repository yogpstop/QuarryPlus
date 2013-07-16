package org.yogpstop.qp;

import java.util.ArrayList;

import buildcraft.api.tools.IToolWrench;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeDirection;

public class BlockQuarry extends BlockContainer {
	Icon textureTop, textureFront, texBB, texNNB, texMF, texF;

	public BlockQuarry(int i) {
		super(i, Material.iron);
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(tabBuildCraft);
		setUnlocalizedName("QuarryPlus");
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		this.drop.clear();
		if (world.isRemote) return;
		TileQuarry tq = (TileQuarry) world.getBlockTileEntity(x, y, z);
		int count = quantityDropped(meta, 0, world.rand);
		int id1 = idDropped(meta, world.rand, 0);
		if (id1 > 0) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(id1, 1, damageDropped(meta));
				tq.S_setEnchantment(is);
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
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess ba, int x, int y, int z, int side) {
		TileEntity tile = ba.getBlockTileEntity(x, y, z);
		if (tile instanceof TileQuarry) {
			if (side == 1) {
				switch (((TileQuarry) tile).G_getNow()) {
				case TileQuarry.BREAKBLOCK:
				case TileQuarry.MOVEHEAD:
					return this.texBB;
				case TileQuarry.WAITLIQUID:
					return this.texF;
				case TileQuarry.MAKEFRAME:
					return this.texMF;
				case TileQuarry.NOTNEEDBREAK:
					return this.texNNB;
				}
			}
		}
		return super.getBlockTexture(ba, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int i, int j) {
		if (j == 0 && i == 3) return this.textureFront;

		if (i == j) return this.textureFront;

		switch (i) {
		case 1:
			return this.textureTop;
		default:
			return this.blockIcon;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry");
		this.textureTop = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry_top");
		this.textureFront = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry_front");
		this.texBB = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry_top_bb");
		this.texNNB = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry_top_nnb");
		this.texMF = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry_top_mf");
		this.texF = par1IconRegister.registerIcon("yogpstop/quarryplus:quarry_top_f");
	}

	@Override
	public TileEntity createNewTileEntity(World w) {
		return new TileQuarry();
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el, ItemStack stack) {
		super.onBlockPlacedBy(w, x, y, z, el, stack);
		ForgeDirection orientation = get2dOrientation(el.posX, el.posZ, x, z);
		w.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 1);
		((TileQuarry) w.getBlockTileEntity(x, y, z)).G_init(stack.getEnchantmentTagList());
	}

	private static ForgeDirection get2dOrientation(double x1, double z1, double x2, double z2) {
		double Dx = x1 - x2;
		double Dz = z1 - z2;
		double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315) return ForgeDirection.EAST;
		else if (angle < 135) return ForgeDirection.SOUTH;
		else if (angle < 225) return ForgeDirection.WEST;
		else return ForgeDirection.NORTH;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			((TileQuarry) world.getBlockTileEntity(x, y, z)).G_reinit();
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		return false;
	}

}