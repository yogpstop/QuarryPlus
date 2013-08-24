package org.yogpstop.qp;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockInfMJSrc extends BlockContainer {

	public BlockInfMJSrc(int par1) {
		super(par1, Material.iron);
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(tabBuildCraft);
		setUnlocalizedName("InfMJSrc");
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileInfMJSrc();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) return true;
		ep.openGui(QuarryPlus.instance, QuarryPlus.guiIdInfMJSrc, world, x, y, z);
		return true;
	}
}
