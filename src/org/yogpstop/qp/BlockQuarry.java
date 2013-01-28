package org.yogpstop.qp;

import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockQuarry extends BlockContainer {
	int textureTop;
	int textureFront;
	int textureSide;

	public BlockQuarry(int i) {
		super(i, Material.iron);
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);
		textureSide = 3;
		textureFront = 1;
		textureTop = 2;
		setCreativeTab(tabBuildCraft);
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (j == 0 && i == 3)
			return textureFront;

		if (i == j)
			return textureFront;

		switch (i) {
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World w) {
		return new TileQuarry();
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el) {
		super.onBlockPlacedBy(w, x, y, z, el);
		ForgeDirection orientation = get2dOrientation(el.posX, el.posZ, x, z);
		w.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite()
				.ordinal());
		((TileQuarry) w.getBlockTileEntity(x, y, z)).init(el);
	}

	private static ForgeDirection get2dOrientation(double x1, double z1,
			double x2, double z2) {
		double Dx = x1 - x2;
		double Dz = z1 - z2;
		double angle = Math.atan2(Dz, Dx) / Math.PI * 180 + 180;

		if (angle < 45 || angle > 315)
			return ForgeDirection.EAST;
		else if (angle < 135)
			return ForgeDirection.SOUTH;
		else if (angle < 225)
			return ForgeDirection.WEST;
		else
			return ForgeDirection.NORTH;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer entityplayer, int par6, float par7, float par8,
			float par9) {
		if (entityplayer.isSneaking())
			return false;
		entityplayer.openGui(QuarryPlus.instance,
				QuarryPlus.guiIdContainerQuarry, world, x, y, z);
		return true;
	}

	@Override
	public String getTextureFile() {
		return "/org/yogpstop/qp/blocks.png";
	}
}