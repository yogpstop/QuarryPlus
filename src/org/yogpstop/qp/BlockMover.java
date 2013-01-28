package org.yogpstop.qp;

import buildcraft.core.CreativeTabBuildCraft;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class BlockMover extends Block {
	int textureTop;
	int textureBottom;
	int textureSide;

	public BlockMover(int par1) {
		super(par1, Material.iron);
		textureSide = 9;
		textureBottom = 11;
		textureTop = 10;
		this.setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public String getTextureFile() {
		return "/org/yogpstop/qp/blocks.png";
	}

	@Override
	public int getBlockTextureFromSide(int i) {

		switch (i) {
		case 1:
			return textureTop;
		case 0:
			return textureBottom;
		default:
			return textureSide;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer entityPlayer, int par6, float par7, float par8,
			float par9) {
		if (world.isRemote) {
			return true;
		} else {
			entityPlayer.openGui(QuarryPlus.instance,
					QuarryPlus.guiIdContainerMover, world, x, y, z);
			return true;
		}

	}
}
