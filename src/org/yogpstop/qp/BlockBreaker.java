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

import static buildcraft.core.utils.Utils.addToRandomInventoryAround;
import static buildcraft.core.utils.Utils.addToRandomPipeAround;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.proxy.CoreProxy;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
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
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockBreaker extends BlockContainer {
	@SideOnly(Side.CLIENT)
	protected Icon furnaceTopIcon;
	@SideOnly(Side.CLIENT)
	protected Icon horizontal;
	@SideOnly(Side.CLIENT)
	protected Icon vectrial;

	protected BlockBreaker(int par1) {
		super(par1, Material.rock);
		this.setCreativeTab(CreativeTabs.tabRedstone);
		this.setHardness(3.5F);
		this.setStepSound(soundStoneFootstep);
		this.setUnlocalizedName("BreakerPlus");
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
		this.horizontal = par1IconRegister.registerIcon("yogpstop_qp:breaker_front_horizontal");
		this.vectrial = par1IconRegister.registerIcon("yogpstop_qp:breaker_front_vertical");
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
		boolean flag = par1World.isBlockIndirectlyGettingPowered(par2, par3, par4) || par1World.isBlockIndirectlyGettingPowered(par2, par3 + 1, par4);
		int i1 = par1World.getBlockMetadata(par2, par3, par4);
		boolean flag1 = (i1 & 8) != 0;
		if (flag && !flag1) {
			updateTick(par1World, par2, par3, par4, par1World.rand);
			par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 | 8, 4);
		} else if (!flag && flag1) {
			par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 & -9, 4);
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random prandom) {
		if (world.isRemote) return;
		TileBreaker tile = (TileBreaker) world.getBlockTileEntity(x, y, z);
		ForgeDirection fd = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z) & 7);
		int tx = x + fd.offsetX, ty = y + fd.offsetY, tz = z + fd.offsetZ, id = world.getBlockId(tx, ty, tz), meta = world.getBlockMetadata(tx, ty, tz);
		if (ty < 1) return;
		if (id <= 0) return;
		Block b = Block.blocksList[id];
		if (b == null) return;
		final EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer(world);
		b.onBlockHarvested(world, tx, ty, tz, meta, player);
		if (b.removeBlockByPlayer(world, player, tx, ty, tz)) b.onBlockDestroyedByPlayer(world, tx, ty, tz, meta);
		else return;
		ArrayList<ItemStack> alis;
		if (b.canSilkHarvest(world, player, tx, ty, tz, meta) && tile.silktouch) {
			alis = new ArrayList<ItemStack>();
			try {
				ItemStack is = (ItemStack) TileBasic.createStackedBlock.invoke(b, meta);
				if (is != null) alis.add(is);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} else {
			alis = b.getBlockDropped(world, tx, ty, tz, meta, tile.fortune);
		}
		for (ItemStack is : alis) {
			int added = addToRandomInventoryAround(world, x, y, z, is);
			is.stackSize -= added;
			if (is.stackSize > 0) {
				added = addToRandomPipeAround(world, x, y, z, ForgeDirection.UNKNOWN, is);
				is.stackSize -= added;
				if (is.stackSize > 0) {
					float f = 0.7F;
					double d0 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					double d1 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					double d2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					EntityItem entityitem = new EntityItem(world, x + d0, y + d1, z + d2, is);
					entityitem.delayBeforeCanPickup = 10;
					world.spawnEntityInWorld(entityitem);
				}
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase ent, ItemStack is) {
		EnchantmentHelper.init((IEnchantableTile) world.getBlockTileEntity(x, y, z), is.getEnchantmentTagList());
		world.setBlockMetadataWithNotify(x, y, z, BlockPistonBase.determineOrientation(world, x, y, z, ent), 2);
	}

	static void setDispenserDefaultDirection(World par1World, int par2, int par3, int par4) {
		if (!par1World.isRemote) {
			int l = par1World.getBlockId(par2, par3, par4 - 1);
			int i1 = par1World.getBlockId(par2, par3, par4 + 1);
			int j1 = par1World.getBlockId(par2 - 1, par3, par4);
			int k1 = par1World.getBlockId(par2 + 1, par3, par4);
			byte b0 = 3;

			if (Block.opaqueCubeLookup[l] && !Block.opaqueCubeLookup[i1]) {
				b0 = 3;
			}

			if (Block.opaqueCubeLookup[i1] && !Block.opaqueCubeLookup[l]) {
				b0 = 2;
			}

			if (Block.opaqueCubeLookup[j1] && !Block.opaqueCubeLookup[k1]) {
				b0 = 5;
			}

			if (Block.opaqueCubeLookup[k1] && !Block.opaqueCubeLookup[j1]) {
				b0 = 4;
			}
			par1World.setBlockMetadataWithNotify(par2, par3, par4, b0, 2);
		}
	}

	private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		TileBreaker tile = (TileBreaker) world.getBlockTileEntity(x, y, z);
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
	public TileEntity createNewTileEntity(World world) {
		return new TileBreaker();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int side, float par7, float par8, float par9) {
		Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
			int i = world.getBlockMetadata(x, y, z) + 1;
			if (i >= 6) i = 0;
			world.setBlockMetadataWithNotify(x, y, z, i, 2);
			((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
			return true;
		}
		if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
			if (world.isRemote) return true;
			for (String s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world.getBlockTileEntity(x, y, z)))
				PacketDispatcher.sendPacketToPlayer(new Packet3Chat(ChatMessageComponent.createFromText(s)), (Player) ep);
			return true;
		}
		return false;
	}
}
