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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMover extends Block {
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
  public boolean onBlockActivated(final World world, final int x, final int y, final int z,
      final EntityPlayer ep, final int par6, final float par7, final float par8, final float par9) {
    if (world.isRemote)
      return true;
    ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdMover, world, x, y, z);
    return true;
  }
}
