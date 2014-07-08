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

import buildcraft.BuildCraftCore;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockMarker extends BlockContainer {

	public BlockMarker() {
		super(Material.circuits);
		setLightLevel(0.5F);
		setCreativeTab(QuarryPlus.ct);
		setBlockName("MarkerPlus");
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.markerModel;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		float w = 0.15F;
		float h = 0.65F;
		ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
		switch (dir) {
		case DOWN:
			this.setBlockBounds(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
			break;
		case UP:
			this.setBlockBounds(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
			break;
		case SOUTH:
			this.setBlockBounds(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
			break;
		case NORTH:
			this.setBlockBounds(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
			break;
		case EAST:
			this.setBlockBounds(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
			break;
		default:
			this.setBlockBounds(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
			break;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		return world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir.getOpposite());
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		return side;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		dropTorchIfCantStay(world, x, y, z);
	}

	private void dropTorchIfCantStay(World w, int x, int y, int z) {
		int m = w.getBlockMetadata(x, y, z);
		if (!canPlaceBlockOnSide(w, x, y, z, m)) {
			dropBlockAsItem(w, x, y, z, m, 0);
			w.setBlockToAir(x, y, z);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World w, int m) {
		return new TileMarker();
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block b) {
		((TileMarker) w.getTileEntity(x, y, z)).G_updateSignal();
		dropTorchIfCantStay(w, x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) {
			Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
				TileMarker.Link l = ((TileMarker) world.getTileEntity(x, y, z)).link;
				if (l == null) return true;
				ep.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.markerarea")));
				ep.addChatMessage(new ChatComponentText(String.format("x:%d y:%d z:%d - x:%d y:%d z:%d", l.xn, l.yn, l.zn, l.xx, l.yx, l.zx)));
				return true;
			}
			((TileMarker) world.getTileEntity(x, y, z)).S_tryConnection();
		}
		return true;
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
		((TileMarker) world.getTileEntity(x, y, z)).requestTicket();
		super.onPostBlockPlaced(world, x, y, z, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:marker");
	}
}
