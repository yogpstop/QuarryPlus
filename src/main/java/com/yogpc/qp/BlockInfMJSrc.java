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

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInfMJSrc extends BlockContainer {

  public BlockInfMJSrc() {
    super(Material.iron);
    setHardness(1.5F);
    setResistance(10F);
    setStepSound(soundTypeStone);
    setCreativeTab(QuarryPlus.ct);
    setBlockName("InfMJSrc");
    setBlockTextureName("portal");
  }

  @Override
  public TileEntity createNewTileEntity(final World w, final int m) {
    return new TileInfMJSrc();
  }

  @Override
  public boolean onBlockActivated(final World w, final int x, final int y, final int z,
      final EntityPlayer ep, final int par6, final float par7, final float par8, final float par9) {
    if (!w.isRemote)
      ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdInfMJSrc, w, x, y, z);
    return true;
  }
}
