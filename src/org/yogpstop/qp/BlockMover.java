package org.yogpstop.qp;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockMover extends Block {
	Icon textureTop;
	Icon textureBottom;

	public BlockMover(int par1) {
		super(par1, Material.iron);
		this.setCreativeTab(tabBuildCraft);
	}

	@Override
	public Icon getBlockTextureFromSideAndMetadata(int i, int j) {

		switch (i) {
		case 1:
			return textureTop;
		case 0:
			return textureBottom;
		default:
			return field_94336_cN;
		}
	}

	@SideOnly(Side.CLIENT)
	public void func_94332_a(IconRegister par1IconRegister) {
		this.field_94336_cN = par1IconRegister
				.func_94245_a("yogpstop/quarryplus:mover_side");
		this.textureTop = par1IconRegister
				.func_94245_a("yogpstop/quarryplus:mover_top");
		this.textureBottom = par1IconRegister
				.func_94245_a("yogpstop/quarryplus:mover_bottom");
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
