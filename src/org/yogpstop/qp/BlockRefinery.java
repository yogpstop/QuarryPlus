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

import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockRefinery extends BlockContainer {

	public BlockRefinery(int par1) {
		super(par1, Material.iron);
		setHardness(5F);
		setCreativeTab(QuarryPlus.ct);
		setUnlocalizedName("RefineryPlus");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileRefinery();
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		this.drop.clear();
		TileRefinery tile = (TileRefinery) world.getBlockTileEntity(x, y, z);
		if (world.isRemote || tile == null) return;
		int count = quantityDropped(meta, 0, world.rand);
		int id1 = idDropped(meta, world.rand, 0);
		if (id1 > 0) {
			for (int i = 0; i < count; i++) {
				ItemStack is = new ItemStack(id1, 1, damageDropped(meta));
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
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase el, ItemStack is) {
		super.onBlockPlacedBy(w, x, y, z, el, is);
		EnchantmentHelper.init((IEnchantableTile) w.getBlockTileEntity(x, y, z), is.getEnchantmentTagList());
		ForgeDirection orientation = Utils.get2dOrientation(new Position(el.posX, el.posY, el.posZ), new Position(x, y, z));
		w.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			if (world.isRemote) return true;
			int meta = world.getBlockMetadata(x, y, z);
			switch (ForgeDirection.values()[meta]) {
			case WEST:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.SOUTH.ordinal(), 3);
				break;
			case EAST:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.NORTH.ordinal(), 3);
				break;
			case NORTH:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.WEST.ordinal(), 3);
				break;
			case SOUTH:
			default:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.EAST.ordinal(), 3);
				break;
			}
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		} else if (equipped instanceof ItemTool) {
			if (ep.getCurrentEquippedItem().getItemDamage() == 0) {
				if (world.isRemote) return true;
				for (String s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world.getBlockTileEntity(x, y, z)))
					PacketDispatcher.sendPacketToPlayer(new Packet3Chat(ChatMessageComponent.createFromText(s)), (Player) ep);
				return true;
			}
		} else {
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(ep.getCurrentEquippedItem());
			if (liquid != null) {
				if (world.isRemote) return true;
				int qty = ((TileRefinery) world.getBlockTileEntity(x, y, z)).fill(ForgeDirection.UNKNOWN, liquid, true);
				if (qty != 0 && !ep.capabilities.isCreativeMode) {
					ep.inventory.setInventorySlotContents(ep.inventory.currentItem, Utils.consumeItem(ep.inventory.getCurrentItem()));
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int getRenderType() {
		return QuarryPlus.refineryRenderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}
