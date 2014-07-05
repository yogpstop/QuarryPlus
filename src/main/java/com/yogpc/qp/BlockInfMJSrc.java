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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockInfMJSrc extends BlockContainer {

	public BlockInfMJSrc() {
		super(Material.iron);
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundTypeStone);
		setCreativeTab(QuarryPlus.ct);
		setBlockName("InfMJSrc");
	}

	@Override
	public TileEntity createNewTileEntity(World w, int m) {
		return new TileInfMJSrc();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) ((TileInfMJSrc) world.getTileEntity(x, y, z)).S_openGUI(ep);
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int a, int b) {
		return Blocks.portal.getIcon(a, b);
	}
}
