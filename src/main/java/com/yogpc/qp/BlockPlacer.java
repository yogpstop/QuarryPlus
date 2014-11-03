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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

import com.yogpc.qp.bc.BuildCraftHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockPlacer extends BlockContainer {
  private final Random random = new Random();
  @SideOnly(Side.CLIENT)
  protected IIcon furnaceTopIcon;
  @SideOnly(Side.CLIENT)
  protected IIcon horizontal;
  @SideOnly(Side.CLIENT)
  protected IIcon vectrial;

  protected BlockPlacer() {
    super(Material.rock);
    setCreativeTab(QuarryPlus.ct);
    setHardness(3.5F);
    setStepSound(soundTypeStone);
    setBlockName("PlacerPlus");
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
    this.horizontal = par1IconRegister.registerIcon("yogpstop_qp:placer_front_horizontal");
    this.vectrial = par1IconRegister.registerIcon("yogpstop_qp:placer_front_vertical");
  }

  @Override
  public void onNeighborBlockChange(final World par1World, final int par2, final int par3,
      final int par4, final Block par5) {
    final boolean flag =
        par1World.isBlockIndirectlyGettingPowered(par2, par3, par4)
            || par1World.isBlockIndirectlyGettingPowered(par2, par3 + 1, par4);
    final int i1 = par1World.getBlockMetadata(par2, par3, par4);
    final boolean flag1 = (i1 & 8) != 0;

    if (flag && !flag1) {
      par1World.scheduleBlockUpdate(par2, par3, par4, this, 0);
      par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 | 8, 4);
    } else if (!flag && flag1)
      par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 & -9, 4);
  }

  @Override
  public void updateTick(final World world, final int x, final int y, final int z,
      final Random prandom) {
    if (world.isRemote)
      return;
    final TilePlacer tile = (TilePlacer) world.getTileEntity(x, y, z);

    if (tile != null) {
      int tx, ty, tz, sd1, sd2, sd3, sd4, sd5, sd6;
      ForgeDirection fd1, fd2, fd3, fd4, fd5, fd6;

      sd2 = world.getBlockMetadata(x, y, z) & 7;
      fd2 = ForgeDirection.getOrientation(sd2);
      fd1 = fd2.getOpposite();
      sd1 = fd1.ordinal();

      sd3 = sd1 + 2;
      if (sd3 >= 6)
        sd3 -= 6;
      fd3 = ForgeDirection.getOrientation(sd3);
      fd4 = fd3.getOpposite();
      sd4 = fd4.ordinal();

      sd5 = sd3 + 2;
      if (sd5 >= 6)
        sd5 -= 6;
      fd5 = ForgeDirection.getOrientation(sd5);
      fd6 = fd5.getOpposite();
      sd6 = fd6.ordinal();

      tx = x + fd2.offsetX;
      ty = y + fd2.offsetY;
      tz = z + fd2.offsetZ;

      final EntityPlayer player = FakePlayerFactory.getMinecraft((WorldServer) world);
      ItemStack is = null;
      int i = 0;
      for (; i < tile.getSizeInventory(); i++) {
        is = tile.getStackInSlot(i);
        if (is != null && is.getItem() != null) {
          if (is.getItem().onItemUseFirst(is, player, world, tx, ty, tz, sd1, 0.5F, 0.5F, 0.5F))
            break;
          if (is.getItem().onItemUseFirst(is, player, world, tx + fd1.offsetX, ty + fd1.offsetY,
              tz + fd1.offsetZ, sd2, 0.5F, 0.5F, 0.5F))
            break;
          if (is.getItem().onItemUseFirst(is, player, world, tx + fd2.offsetX, ty + fd2.offsetY,
              tz + fd2.offsetZ, sd1, 0.5F, 0.5F, 0.5F))
            break;
          if (is.getItem().onItemUseFirst(is, player, world, tx + fd3.offsetX, ty + fd3.offsetY,
              tz + fd3.offsetZ, sd4, 0.5F, 0.5F, 0.5F))
            break;
          if (is.getItem().onItemUseFirst(is, player, world, tx + fd4.offsetX, ty + fd4.offsetY,
              tz + fd4.offsetZ, sd3, 0.5F, 0.5F, 0.5F))
            break;
          if (is.getItem().onItemUseFirst(is, player, world, tx + fd5.offsetX, ty + fd5.offsetY,
              tz + fd5.offsetZ, sd6, 0.5F, 0.5F, 0.5F))
            break;
          if (is.getItem().onItemUseFirst(is, player, world, tx + fd6.offsetX, ty + fd6.offsetY,
              tz + fd6.offsetZ, sd5, 0.5F, 0.5F, 0.5F))
            break;
        }
        final Block k = world.getBlock(tx, ty, tz);
        if (k != null && k.onBlockActivated(world, tx, ty, tz, player, sd1, 0.5F, 0.5F, 0.5F))
          break;
        if (is != null) {
          if (is.tryPlaceItemIntoWorld(player, world, tx, ty, tz, sd1, 0.5F, 0.5F, 0.5F))
            break;
          if (is.tryPlaceItemIntoWorld(player, world, tx + fd1.offsetX, ty + fd1.offsetY, tz
              + fd1.offsetZ, sd2, 0.5F, 0.5F, 0.5F))
            break;
          if (is.tryPlaceItemIntoWorld(player, world, tx + fd2.offsetX, ty + fd2.offsetY, tz
              + fd2.offsetZ, sd1, 0.5F, 0.5F, 0.5F))
            break;
          if (is.tryPlaceItemIntoWorld(player, world, tx + fd3.offsetX, ty + fd3.offsetY, tz
              + fd3.offsetZ, sd4, 0.5F, 0.5F, 0.5F))
            break;
          if (is.tryPlaceItemIntoWorld(player, world, tx + fd4.offsetX, ty + fd4.offsetY, tz
              + fd4.offsetZ, sd3, 0.5F, 0.5F, 0.5F))
            break;
          if (is.tryPlaceItemIntoWorld(player, world, tx + fd5.offsetX, ty + fd5.offsetY, tz
              + fd5.offsetZ, sd6, 0.5F, 0.5F, 0.5F))
            break;
          if (is.tryPlaceItemIntoWorld(player, world, tx + fd6.offsetX, ty + fd6.offsetY, tz
              + fd6.offsetZ, sd5, 0.5F, 0.5F, 0.5F))
            break;
        }
      }
      if (is != null && is.stackSize <= 0)
        tile.setInventorySlotContents(i, null);
    }
  }

  @Override
  public void onBlockPlacedBy(final World par1World, final int par2, final int par3,
      final int par4, final EntityLivingBase par5EntityLivingBase, final ItemStack par6ItemStack) {
    final int l =
        BlockPistonBase.determineOrientation(par1World, par2, par3, par4, par5EntityLivingBase);
    par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
  }

  @Override
  public void breakBlock(final World world, final int x, final int y, final int z, final Block b,
      final int meta) {
    final TilePlacer tile = (TilePlacer) world.getTileEntity(x, y, z);

    if (tile != null) {
      for (int j1 = 0; j1 < tile.getSizeInventory(); ++j1) {
        final ItemStack itemstack = tile.getStackInSlot(j1);

        if (itemstack != null) {
          final float f = this.random.nextFloat() * 0.8F + 0.1F;
          final float f1 = this.random.nextFloat() * 0.8F + 0.1F;
          final float f2 = this.random.nextFloat() * 0.8F + 0.1F;

          while (itemstack.stackSize > 0) {
            int k1 = this.random.nextInt(21) + 10;

            if (k1 > itemstack.stackSize)
              k1 = itemstack.stackSize;

            itemstack.stackSize -= k1;
            final EntityItem entityitem =
                new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(itemstack.getItem(), k1,
                    itemstack.getItemDamage()));

            if (itemstack.hasTagCompound())
              entityitem.getEntityItem().setTagCompound(
                  (NBTTagCompound) itemstack.getTagCompound().copy());

            final float f3 = 0.05F;
            entityitem.motionX = (float) this.random.nextGaussian() * f3;
            entityitem.motionY = (float) this.random.nextGaussian() * f3 + 0.2F;
            entityitem.motionZ = (float) this.random.nextGaussian() * f3;
            world.spawnEntityInWorld(entityitem);
          }
        }
      }

      world.func_147453_f(x, y, z, b);
    }

    super.breakBlock(world, x, y, z, b, meta);
  }

  @Override
  public TileEntity createNewTileEntity(final World w, final int m) {
    return new TilePlacer();
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z,
      final EntityPlayer ep, final int par6, final float par7, final float par8, final float par9) {
    final Item equipped =
        ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
    if (BuildCraftHelper.isWrench(equipped, ep, x, y, z)) {
      int i = world.getBlockMetadata(x, y, z) + 1;
      if (i >= 6)
        i = 0;
      world.setBlockMetadataWithNotify(x, y, z, i, 2);
      return true;
    }
    if (!world.isRemote)
      ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdPlacer, world, x, y, z);
    return true;
  }
}
