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

import java.util.Random;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockPlacer extends BlockContainer {
	private final Random random = new Random();
	@SideOnly(Side.CLIENT)
	protected Icon furnaceTopIcon;
	@SideOnly(Side.CLIENT)
	protected Icon horizontal;
	@SideOnly(Side.CLIENT)
	protected Icon vectrial;

	protected BlockPlacer(int par1) {
		super(par1, Material.rock);
		this.setCreativeTab(CreativeTabs.tabRedstone);
		this.setHardness(3.5F);
		this.setStepSound(soundStoneFootstep);
		this.setUnlocalizedName("PlacerPlus");
	}

	@Override
	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
		super.onBlockAdded(par1World, par2, par3, par4);
		BlockBreaker.setDispenserDefaultDirection(par1World, par2, par3, par4);
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
		int out = 7 & world.getBlockMetadata(x, y, z);
		return out != side.ordinal();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
		int k = par2 & 7;
		return par1 == k ? (k != 1 && k != 0 ? this.horizontal : this.vectrial) : (k != 1 && k != 0 ? (par1 != 1 && par1 != 0 ? this.blockIcon
				: this.furnaceTopIcon) : this.furnaceTopIcon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:plusstone_side");
		this.furnaceTopIcon = par1IconRegister.registerIcon("yogpstop_qp:plusstone_top");
		this.horizontal = par1IconRegister.registerIcon("yogpstop_qp:placer_front_horizontal");
		this.vectrial = par1IconRegister.registerIcon("yogpstop_qp:placer_front_vertical");
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
		boolean flag = par1World.isBlockIndirectlyGettingPowered(par2, par3, par4) || par1World.isBlockIndirectlyGettingPowered(par2, par3 + 1, par4);
		int i1 = par1World.getBlockMetadata(par2, par3, par4);
		boolean flag1 = (i1 & 8) != 0;

		if (flag && !flag1) {
			par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, 0);
			par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 | 8, 4);
		} else if (!flag && flag1) {
			par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 & -9, 4);
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random prandom) {
		if (world.isRemote) return;
		TilePlacer tile = (TilePlacer) world.getBlockTileEntity(x, y, z);

		if (tile != null) {
			int i = 0, j = tile.getSizeInventory(), tx, ty, tz, sd1, sd2, sd3, sd4, sd5, sd6;
			ForgeDirection fd1, fd2, fd3, fd4, fd5, fd6;
			sd2 = world.getBlockMetadata(x, y, z) & 7;
			fd2 = ForgeDirection.getOrientation(sd2);
			fd1 = fd2.getOpposite();
			sd1 = fd1.ordinal();

			sd3 = sd1 + 2;
			if (sd3 >= 6) sd3 -= 6;
			fd3 = ForgeDirection.getOrientation(sd3);
			fd4 = fd3.getOpposite();
			sd4 = fd4.ordinal();

			sd5 = sd3 + 2;
			if (sd5 >= 6) sd5 -= 6;
			fd5 = ForgeDirection.getOrientation(sd5);
			fd6 = fd5.getOpposite();
			sd6 = fd6.ordinal();

			tx = x + fd2.offsetX;
			ty = y + fd2.offsetY;
			tz = z + fd2.offsetZ;
			ItemStack is;
			int k;
			final EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(world);
			for (; i < j; i++) {
				is = tile.getStackInSlot(i);
				if (is == null || is.getItem().shouldPassSneakingClickToBlock(world, tx, ty, tz)) {
					k = world.getBlockId(tx, ty, tz);
					if (k > 0 && Block.blocksList[k].onBlockActivated(world, tx, ty, tz, player, sd1, 0.5F, 0.5F, 0.5F)) break;
				} else {
					if (is.getItem().onItemUseFirst(is, player, world, tx, ty, tz, sd1, 0.5F, 0.5F, 0.5F)) break;
					if (is.getItem().onItemUseFirst(is, player, world, tx + fd1.offsetX, ty + fd1.offsetY, tz + fd1.offsetZ, sd2, 0.5F, 0.5F, 0.5F)) break;
					if (is.getItem().onItemUseFirst(is, player, world, tx + fd2.offsetX, ty + fd2.offsetY, tz + fd2.offsetZ, sd1, 0.5F, 0.5F, 0.5F)) break;
					if (is.getItem().onItemUseFirst(is, player, world, tx + fd3.offsetX, ty + fd3.offsetY, tz + fd3.offsetZ, sd4, 0.5F, 0.5F, 0.5F)) break;
					if (is.getItem().onItemUseFirst(is, player, world, tx + fd4.offsetX, ty + fd4.offsetY, tz + fd4.offsetZ, sd3, 0.5F, 0.5F, 0.5F)) break;
					if (is.getItem().onItemUseFirst(is, player, world, tx + fd5.offsetX, ty + fd5.offsetY, tz + fd5.offsetZ, sd6, 0.5F, 0.5F, 0.5F)) break;
					if (is.getItem().onItemUseFirst(is, player, world, tx + fd6.offsetX, ty + fd6.offsetY, tz + fd6.offsetZ, sd5, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx, ty, tz, sd1, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx + fd1.offsetX, ty + fd1.offsetY, tz + fd1.offsetZ, sd2, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx + fd2.offsetX, ty + fd2.offsetY, tz + fd2.offsetZ, sd1, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx + fd3.offsetX, ty + fd3.offsetY, tz + fd3.offsetZ, sd4, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx + fd4.offsetX, ty + fd4.offsetY, tz + fd4.offsetZ, sd3, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx + fd5.offsetX, ty + fd5.offsetY, tz + fd5.offsetZ, sd6, 0.5F, 0.5F, 0.5F)) break;
					if (is.tryPlaceItemIntoWorld(player, world, tx + fd6.offsetX, ty + fd6.offsetY, tz + fd6.offsetZ, sd5, 0.5F, 0.5F, 0.5F)) break;
					if (is.stackSize <= 0) tile.setInventorySlotContents(i, null);
				}
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World par1World, int par2, int par3, int par4, EntityLivingBase par5EntityLivingBase, ItemStack par6ItemStack) {
		int l = BlockPistonBase.determineOrientation(par1World, par2, par3, par4, par5EntityLivingBase);
		par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		TilePlacer tile = (TilePlacer) world.getBlockTileEntity(x, y, z);

		if (tile != null) {
			for (int j1 = 0; j1 < tile.getSizeInventory(); ++j1) {
				ItemStack itemstack = tile.getStackInSlot(j1);

				if (itemstack != null) {
					float f = this.random.nextFloat() * 0.8F + 0.1F;
					float f1 = this.random.nextFloat() * 0.8F + 0.1F;
					float f2 = this.random.nextFloat() * 0.8F + 0.1F;

					while (itemstack.stackSize > 0) {
						int k1 = this.random.nextInt(21) + 10;

						if (k1 > itemstack.stackSize) {
							k1 = itemstack.stackSize;
						}

						itemstack.stackSize -= k1;
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(itemstack.itemID, k1, itemstack.getItemDamage()));

						if (itemstack.hasTagCompound()) {
							entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (float) this.random.nextGaussian() * f3;
						entityitem.motionY = (float) this.random.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) this.random.nextGaussian() * f3;
						world.spawnEntityInWorld(entityitem);
					}
				}
			}

			world.func_96440_m(x, y, z, id);
		}

		super.breakBlock(world, x, y, z, id, meta);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TilePlacer();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			int i = world.getBlockMetadata(x, y, z) + 1;
			if (i >= 6) i = 0;
			world.setBlockMetadataWithNotify(x, y, z, i, 2);
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		if (!world.isRemote) ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdPlacer, world, x, y, z);
		return true;
	}
}
