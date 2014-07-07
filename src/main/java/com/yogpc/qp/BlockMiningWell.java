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

import java.util.ArrayList;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.CreativeTabBuildCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockMiningWell extends BlockContainer {
	IIcon textureFront, textureBack, textureTop, texW;

	public BlockMiningWell() {
		super(Material.ground);
		setHardness(1.5F);
		setResistance(10F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
		setStepSound(soundTypeStone);
		setBlockName("MiningWellPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World w, int m) {
		return new TileMiningWell();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.textureFront = par1IconRegister.registerIcon("yogpstop_qp:miningwell_front");
		this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:miningwell");
		this.textureBack = par1IconRegister.registerIcon("yogpstop_qp:miningwell_back");
		this.textureTop = par1IconRegister.registerIcon("yogpstop_qp:miningwell_top");
		this.texW = par1IconRegister.registerIcon("yogpstop_qp:miningwell_top_w");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess ba, int x, int y, int z, int side) {
		TileEntity tile = ba.getTileEntity(x, y, z);
		if (tile instanceof TileMiningWell && side == 1 && ((TileMiningWell) tile).isActive()) return this.texW;
		return super.getIcon(ba, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int i, int j) {
		if (j == 0 && i == 3) return this.textureFront;
		if (i == 1) return this.textureTop;
		else if (i == 0) return this.textureBack;
		else if (i == j) return this.textureFront;
		else if (j >= 0 && j < 6 && ForgeDirection.values()[j].getOpposite().ordinal() == i) return this.textureBack;
		else return this.blockIcon;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase el, ItemStack is) {
		super.onBlockPlacedBy(w, x, y, z, el, is);
		ForgeDirection orientation = get2dOrientation(el.posX, el.posZ, x, z);
		w.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 1);
		EnchantmentHelper.init((IEnchantableTile) w.getTileEntity(x, y, z), is.getEnchantmentTagList());
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

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int meta) {
		this.drop.clear();
		TileMiningWell tile = (TileMiningWell) world.getTileEntity(x, y, z);
		if (world.isRemote || tile == null) return;
		int count = quantityDropped(meta, 0, world.rand);
		Item it = getItemDropped(meta, world.rand, 0);
		if (it != null) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(it, 1, damageDropped(meta));
				EnchantmentHelper.enchantmentToIS(tile, is);
				this.drop.add(is);
			}
		}
		super.breakBlock(world, x, y, z, b, meta);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return this.drop;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			((TileMiningWell) world.getTileEntity(x, y, z)).G_reinit();
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
			if (world.isRemote) return true;
			for (String s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world.getTileEntity(x, y, z)))
				ep.addChatMessage(new ChatComponentText(s));
			return true;
		}
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block b) {
		if (!world.isRemote) ((TileBasic) world.getTileEntity(x, y, z)).G_renew_powerConfigure();
	}
}
