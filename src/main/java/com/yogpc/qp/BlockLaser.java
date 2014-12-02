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

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaser extends BlockContainer {

  @SideOnly(Side.CLIENT)
  private IIcon textureTop, textureBottom;

  public BlockLaser() {
    super(Material.iron);
    setHardness(10F);
    setCreativeTab(QuarryPlus.ct);
    setBlockName("LaserPlus");
  }

  @Override
  public int getRenderType() {
    return QuarryPlus.laserRenderID;
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
  public TileEntity createNewTileEntity(final World w, final int m) {
    return new TileLaser();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(final int i, final int j) {
    if (i == ForgeDirection.values()[j].getOpposite().ordinal())
      return this.textureBottom;
    else if (i == j)
      return this.textureTop;
    else
      return this.blockIcon;
  }

  @Override
  public int onBlockPlaced(final World world, final int x, final int y, final int z,
      final int side, final float par6, final float par7, final float par8, final int meta) {
    super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);
    if (side <= 6)
      return side;
    return meta;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(final IIconRegister par1IconRegister) {
    this.textureTop = par1IconRegister.registerIcon("yogpstop_qp:laser_top");
    this.textureBottom = par1IconRegister.registerIcon("yogpstop_qp:laser_bottom");
    this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:laser_side");
  }

  @Override
  public void onBlockPlacedBy(final World world, final int x, final int y, final int z,
      final EntityLivingBase ent, final ItemStack is) {
    EnchantmentHelper.init((IEnchantableTile) world.getTileEntity(x, y, z),
        is.getEnchantmentTagList());
  }

  private final ArrayList<ItemStack> drop = new ArrayList<ItemStack>();

  @Override
  public void breakBlock(final World world, final int x, final int y, final int z, final Block b,
      final int meta) {
    final TileLaser tile = (TileLaser) world.getTileEntity(x, y, z);
    this.drop.clear();
    final int count = quantityDropped(meta, 0, world.rand);
    ItemStack is;
    for (int i = 0; i < count; i++) {
      final Item it = getItemDropped(meta, world.rand, 0);
      if (it != null) {
        is = new ItemStack(it, 1, damageDropped(meta));
        EnchantmentHelper.enchantmentToIS(tile, is);
        this.drop.add(is);
      }
    }
    super.breakBlock(world, x, y, z, b, meta);
  }

  @Override
  public ArrayList<ItemStack> getDrops(final World world, final int x, final int y, final int z,
      final int metadata, final int fortune) {
    return this.drop;
  }

  @Override
  public boolean onBlockActivated(final World world, final int x, final int y, final int z,
      final EntityPlayer ep, final int side, final float par7, final float par8, final float par9) {
    final Item equipped =
        ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
    if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
      if (!world.isRemote)
        for (final IChatComponent s : EnchantmentHelper
            .getEnchantmentsChat((IEnchantableTile) world.getTileEntity(x, y, z)))
          ep.addChatMessage(s);
      return true;
    }
    return false;
  }
}
