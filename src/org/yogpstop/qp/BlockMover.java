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
		setHardness(1.2F);
		this.setCreativeTab(tabBuildCraft);
		setUnlocalizedName("EnchantMover");
	}

	@Override
	public Icon getIcon(int i, int j) {

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
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("yogpstop/quarryplus:mover");
		this.textureTop = par1IconRegister.registerIcon("yogpstop/quarryplus:mover_top");
		this.textureBottom = par1IconRegister.registerIcon("yogpstop/quarryplus:mover_bottom");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (ep.isSneaking()) return false;
		if (world.isRemote) return true;
		ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdMover, world, x, y, z);
		return true;
	}
}
