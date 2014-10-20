/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

import com.yogpc.mc_lib.InvUtils;
import com.yogpc.mc_lib.ReflectionHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockBreaker extends BlockContainer {
  @SideOnly(Side.CLIENT)
  protected IIcon furnaceTopIcon;
  @SideOnly(Side.CLIENT)
  protected IIcon horizontal;
  @SideOnly(Side.CLIENT)
  protected IIcon vectrial;

  protected BlockBreaker() {
    super(Material.rock);
    setCreativeTab(QuarryPlus.ct);
    setHardness(3.5F);
    setStepSound(soundTypeStone);
    setBlockName("BreakerPlus");
  }

  @Override
  public void onBlockAdded(final World par1World, final int par2, final int par3, final int par4) {
    super.onBlockAdded(par1World, par2, par3, par4);
    BlockBreaker.setDispenserDefaultDirection(par1World, par2, par3, par4);
  }

  @Override
  public boolean isSideSolid(final IBlockAccess world, final int x, final int y, final int z,
      final ForgeDirection side) {
    final int out = 7 & world.getBlockMetadata(x, y, z);
    return out != side.ordinal();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(final int par1, final int par2) {
    final int k = par2 & 7;
    return par1 == k ? k != 1 && k != 0 ? this.horizontal : this.vectrial
        : k != 1 && k != 0 ? par1 != 1 && par1 != 0 ? this.blockIcon : this.furnaceTopIcon
            : this.furnaceTopIcon;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(final IIconRegister par1IconRegister) {
    this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:plusstone_side");
    this.furnaceTopIcon = par1IconRegister.registerIcon("yogpstop_qp:plusstone_top");
    this.horizontal = par1IconRegister.registerIcon("yogpstop_qp:breaker_front_horizontal");
    this.vectrial = par1IconRegister.registerIcon("yogpstop_qp:breaker_front_vertical");
  }

  @Override
  public void onNeighborBlockChange(final World par1World, final int x, final int y, final int z,
      final Block b) {
    final boolean flag =
        par1World.isBlockIndirectlyGettingPowered(x, y, z)
            || par1World.isBlockIndirectlyGettingPowered(x, y + 1, z);
    final int i1 = par1World.getBlockMetadata(x, y, z);
    final boolean flag1 = (i1 & 8) != 0;
    if (flag && !flag1) {
      updateTick(par1World, x, y, z, par1World.rand);
      par1World.setBlockMetadataWithNotify(x, y, z, i1 | 8, 4);
    } else if (!flag && flag1)
      par1World.setBlockMetadataWithNotify(x, y, z, i1 & -9, 4);
  }

  @Override
  public void updateTick(final World w, final int x, final int y, final int z, final Random r) {
    if (w.isRemote)
      return;
    final TileBreaker tile = (TileBreaker) w.getTileEntity(x, y, z);
    final ForgeDirection fd = ForgeDirection.getOrientation(w.getBlockMetadata(x, y, z) & 7);
    final int tx = x + fd.offsetX, ty = y + fd.offsetY, tz = z + fd.offsetZ, meta =
        w.getBlockMetadata(tx, ty, tz);
    if (ty < 1)
      return;
    final Block b = w.getBlock(tx, ty, tz);
    if (b == null || b.isAir(w, x, y, z))
      return;
    final EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer) w);
    b.onBlockHarvested(w, tx, ty, tz, meta, player);
    if (b.removedByPlayer(w, player, tx, ty, tz))
      b.onBlockDestroyedByPlayer(w, tx, ty, tz, meta);
    else
      return;
    ArrayList<ItemStack> alis;
    if (b.canSilkHarvest(w, player, tx, ty, tz, meta) && tile.silktouch) {
      alis = new ArrayList<ItemStack>();
      final ItemStack is =
          (ItemStack) ReflectionHelper.invoke(TileBasic.createStackedBlock, b, new Integer(meta));
      if (is != null)
        alis.add(is);
    } else
      alis = b.getDrops(w, tx, ty, tz, meta, tile.fortune);
    for (final ItemStack is : alis) {
      InvUtils.injectToNearTile(w, x, y, z, is);
      if (is.stackSize > 0) {
        final float f = 0.7F;
        final double d0 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final double d1 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final double d2 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final EntityItem entityitem = new EntityItem(w, x + d0, y + d1, z + d2, is);
        entityitem.delayBeforeCanPickup = 10;
        w.spawnEntityInWorld(entityitem);
      }
    }
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z,
      final EntityLivingBase ent, final ItemStack is) {
    EnchantmentHelper.init((IEnchantableTile) world.getTileEntity(x, y, z),
        is.getEnchantmentTagList());
    world.setBlockMetadataWithNotify(x, y, z,
        BlockPistonBase.determineOrientation(world, x, y, z, ent), 2);
  }

  static void setDispenserDefaultDirection(final World w, final int x, final int y, final int z) {
    if (!w.isRemote) {
      final Block b1 = w.getBlock(x, y, z - 1);
      final Block b2 = w.getBlock(x, y, z + 1);
      final Block b3 = w.getBlock(x - 1, y, z);
      final Block b4 = w.getBlock(x + 1, y, z);
      byte b0 = 3;

      if (b1.func_149730_j() && !b2.func_149730_j())
        b0 = 3;

      if (b2.func_149730_j() && !b1.func_149730_j())
        b0 = 2;

      if (b3.func_149730_j() && !b4.func_149730_j())
        b0 = 5;

      if (b4.func_149730_j() && !b3.func_149730_j())
        b0 = 4;
      w.setBlockMetadataWithNotify(x, y, z, b0, 2);
    }
  }

  private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

  @Override
  public void breakBlock(final World world, final int x, final int y, final int z, final Block id,
      final int meta) {
    final TileBreaker tile = (TileBreaker) world.getTileEntity(x, y, z);
    this.drop.clear();
    final int count = quantityDropped(meta, 0, world.rand);
    ItemStack is;
    for (int i = 0; i < count; i++) {
      final Item id1 = getItemDropped(meta, world.rand, 0);
      if (id1 != null) {
        is = new ItemStack(id1, 1, damageDropped(meta));
        EnchantmentHelper.enchantmentToIS(tile, is);
        this.drop.add(is);
      }
    }
    super.breakBlock(world, x, y, z, id, meta);
  }

  @Override
  public ArrayList<ItemStack> getDrops(final World world, final int x, final int y, final int z,
      final int metadata, final int fortune) {
    return this.drop;
  }

  @Override
  public TileEntity createNewTileEntity(final World w, final int m) {
    return new TileBreaker();
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z,
      final EntityPlayer ep, final int side, final float par7, final float par8, final float par9) {
    final Item equipped =
        ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
    if (BuildCraftHelper.isWrench(equipped, ep, x, y, z)) {
      int i = world.getBlockMetadata(x, y, z) + 1;
      if (i >= 6)
        i = 0;
      world.setBlockMetadataWithNotify(x, y, z, i, 2);
      return true;
    }
    if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
      if (world.isRemote)
        return true;
      for (final IChatComponent s : EnchantmentHelper.getEnchantmentsChat((IEnchantableTile) world
          .getTileEntity(x, y, z)))
        ep.addChatMessage(s);
      return true;
    }
    return false;
  }
}
