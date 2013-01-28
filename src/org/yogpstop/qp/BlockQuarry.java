package org.yogpstop.qp;

import buildcraft.api.core.Position;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
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
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
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
	public void breakBlock(World world, int i, int j, int k, int par5, int par6) {
		((TileQuarry) world.getBlockTileEntity(i, j, k)).destroyQuarry();
		super.breakBlock(world, i, j, k, par5, par6);
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLiving el) {
		super.onBlockPlacedBy(w, x, y, z, el);
		ForgeDirection orientation = Utils.get2dOrientation(new Position(
				el.posX, el.posY, el.posZ), new Position(x, y, z));
		w.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite()
				.ordinal());
		((TileQuarry) w.getBlockTileEntity(x, y, z)).init();
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