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

public class BlockPump extends BlockContainer {

	private IIcon textureTop, textureBottom, textureSide, texW, texC;

	public BlockPump() {
		super(Material.iron);
		setHardness(5F);
		setCreativeTab(QuarryPlus.ct);
		setBlockName("PumpPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World w, int m) {
		return new TilePump();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int i, int j) {
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
	public IIcon getIcon(IBlockAccess ba, int x, int y, int z, int side) {
		TileEntity tile = ba.getTileEntity(x, y, z);
		if (tile instanceof TilePump && side == 1) {
			if (((TilePump) tile).G_working()) return this.texW;
			if (((TilePump) tile).G_connected() != null) return this.texC;
		}
		return super.getIcon(ba, x, y, z, side);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		this.textureTop = par1IconRegister.registerIcon("yogpstop_qp:pump_top");
		this.textureBottom = par1IconRegister.registerIcon("yogpstop_qp:pump_bottom");
		this.textureSide = par1IconRegister.registerIcon("yogpstop_qp:pump_side");
		this.texW = par1IconRegister.registerIcon("yogpstop_qp:pump_top_w");
		this.texC = par1IconRegister.registerIcon("yogpstop_qp:pump_top_c");
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int meta) {
		this.drop.clear();
		TilePump tile = (TilePump) world.getTileEntity(x, y, z);
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
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase el, ItemStack is) {
		super.onBlockPlacedBy(w, x, y, z, el, is);
		EnchantmentHelper.init((IEnchantableTile) w.getTileEntity(x, y, z), is.getEnchantmentTagList());
	}

	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block b) {
		((TilePump) w.getTileEntity(x, y, z)).G_reinit();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (BuildCraftHelper.isWrench(equipped, ep, x, y, z)) {
			if (world.isRemote) return true;
			((TilePump) world.getTileEntity(x, y, z)).S_changeRange(ep);
			return true;
		}
		if (equipped instanceof ItemTool) {
			if (ep.getCurrentEquippedItem().getItemDamage() == 0) {
				if (world.isRemote) return true;
				for (String s : ((TilePump) world.getTileEntity(x, y, z)).C_getNames())
					ep.addChatMessage(new ChatComponentText(s));
				for (String s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world.getTileEntity(x, y, z)))
					ep.addChatMessage(new ChatComponentText(s));
				return true;
			}
			if (ep.getCurrentEquippedItem().getItemDamage() == 2) {
				if (!world.isRemote) ((TilePump) world.getTileEntity(x, y, z)).S_OpenGUI(side, ep);
				return true;
			}
		}
		return false;
	}
}
