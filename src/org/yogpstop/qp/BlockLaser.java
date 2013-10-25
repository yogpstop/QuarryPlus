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

import java.util.ArrayList;

import buildcraft.silicon.SiliconProxy;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockLaser extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private Icon textureTop, textureBottom;

	public BlockLaser(int i) {
		super(i, Material.iron);
		setHardness(10F);
		setCreativeTab(QuarryPlus.ct);
		this.setUnlocalizedName("LaserPlus");
	}

	@Override
	public int getRenderType() {
		return SiliconProxy.laserBlockModel;
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
	public TileEntity createNewTileEntity(World world) {
		return new TileLaser();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int i, int j) {
		if (i == ForgeDirection.values()[j].getOpposite().ordinal()) return this.textureBottom;
		else if (i == j) return this.textureTop;
		else return this.blockIcon;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);
		if (side <= 6) meta = side;
		return meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		this.textureTop = par1IconRegister.registerIcon("yogpstop_qp:laser_top");
		this.textureBottom = par1IconRegister.registerIcon("yogpstop_qp:laser_bottom");
		this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:laser_side");
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase ent, ItemStack is) {
		EnchantmentHelper.init((IEnchantableTile) world.getBlockTileEntity(x, y, z), is.getEnchantmentTagList());
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		TileLaser tile = (TileLaser) world.getBlockTileEntity(x, y, z);
		this.drop.clear();
		int count = quantityDropped(meta, 0, world.rand);
		int id1;
		ItemStack is;
		for (int i = 0; i < count; i++) {
			id1 = idDropped(meta, world.rand, 0);
			if (id1 > 0) {
				is = new ItemStack(id1, 1, damageDropped(meta));
				EnchantmentHelper.enchantmentToIS(tile, is);
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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
			if (world.isRemote) return true;
			for (String s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world.getBlockTileEntity(x, y, z)))
				PacketDispatcher.sendPacketToPlayer(new Packet3Chat(ChatMessageComponent.createFromText(s)), (Player) ep);
			return true;
		}
		return false;
	}
}
