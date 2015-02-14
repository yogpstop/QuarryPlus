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
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cofh.api.block.IDismantleable;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Optional.Interface(iface = "cofh.api.block.IDismantleable", modid = "CoFHAPI|block")
public class BlockMover extends Block implements IDismantleable {
  IIcon textureTop, textureBottom;

  public BlockMover() {
    super(Material.iron);
    setHardness(1.2F);
    setCreativeTab(QuarryPlus.ct);
    setBlockName("EnchantMover");
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(final int i, final int j) {

    switch (i) {
      case 1:
        return this.textureTop;
      case 0:
        return this.textureBottom;
      default:
        return this.blockIcon;
    }
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void registerBlockIcons(final IIconRegister par1IconRegister) {
    this.blockIcon = par1IconRegister.registerIcon("yogpstop_qp:mover");
    this.textureTop = par1IconRegister.registerIcon("yogpstop_qp:mover_top");
    this.textureBottom = par1IconRegister.registerIcon("yogpstop_qp:mover_bottom");
  }

  @Override
  public boolean onBlockActivated(final World w, final int x, final int y, final int z,
      final EntityPlayer e, final int par6, final float par7, final float par8, final float par9) {
    if (!w.isRemote)
      e.openGui(QuarryPlus.instance, QuarryPlus.guiIdMover, w, x, y, z);
    return true;
  }

  @Override
  public boolean canDismantle(final EntityPlayer arg0, final World arg1, final int arg2,
      final int arg3, final int arg4) {
    return true;
  }

  @Override
  public ArrayList<ItemStack> dismantleBlock(final EntityPlayer e, final World w, final int x,
      final int y, final int z, final boolean toinv) {
    final ArrayList<ItemStack> ret = getDrops(w, x, y, z, w.getBlockMetadata(x, y, z), 0);
    w.setBlockToAir(x, y, z);
    if (!toinv)
      for (final ItemStack is : ret) {
        final float f = 0.7F;
        final double d0 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final double d1 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final double d2 = w.rand.nextFloat() * f + (1.0F - f) * 0.5D;
        final EntityItem entityitem = new EntityItem(w, x + d0, y + d1, z + d2, is);
        entityitem.delayBeforeCanPickup = 10;
        w.spawnEntityInWorld(entityitem);
      }
    return ret;
  }
}
