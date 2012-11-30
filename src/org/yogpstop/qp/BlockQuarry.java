package org.yogpstop.qp;

import java.util.ArrayList;
import java.util.Random;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.core.Box;
import buildcraft.core.utils.Utils;
import buildcraft.factory.BlockMachineRoot;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockQuarry extends BlockMachineRoot {
	int textureTop;
	int textureFront;
	int textureSide;

	public BlockQuarry(int blockId) {
		super(blockId, Material.iron);
		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);

		textureSide = 3;
		textureFront = 1;
		textureTop = 2;

		this.setCreativeTab(CreativeTabs.tabRedstone);
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k,
			EntityLiving entityliving) {
		super.onBlockPlacedBy(world, i, j, k, entityliving);

		ForgeDirection orientation = Utils.get2dOrientation(new Position(
				entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite()
				.ordinal());
		if (entityliving instanceof EntityPlayer) {
			TileQuarry tq = (TileQuarry) world.getBlockTileEntity(i, j, k);
			tq.placedBy = (EntityPlayer) entityliving;
		}
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == j) {
			return textureFront;
		}

		switch (i) {
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileQuarry();
	}

	public void searchFrames(World world, int i, int j, int k) {
		int width2 = 1;
		if (!world.checkChunksExist(i - width2, j - width2, k - width2, i
				+ width2, j + width2, k + width2))
			return;

		int blockID = world.getBlockId(i, j, k);

		if (blockID != BuildCraftFactory.frameBlock.blockID)
			return;

		int meta = world.getBlockMetadata(i, j, k);

		if ((meta & 8) == 0) {
			world.setBlockMetadata(i, j, k, meta | 8);

			ForgeDirection[] dirs = ForgeDirection.VALID_DIRECTIONS;

			for (ForgeDirection dir : dirs) {
				switch (dir) {
				case UP:
					searchFrames(world, i, j + 1, k);
				case DOWN:
					searchFrames(world, i, j - 1, k);
				case SOUTH:
					searchFrames(world, i, j, k + 1);
				case NORTH:
					searchFrames(world, i, j, k - 1);
				case EAST:
					searchFrames(world, i + 1, j, k);
				case WEST:
				default:
					searchFrames(world, i - 1, j, k);
				}
			}
		}
	}

	private void markFrameForDecay(World world, int x, int y, int z) {
		if (world.getBlockId(x, y, z) == BuildCraftFactory.frameBlock.blockID) {
			world.setBlockMetadata(x, y, z, 1);
		}
	}

	@Override
	public void breakBlock(World world, int i, int j, int k, int par5, int par6) {

		if (world.isRemote) {
			return;
		}

		TileEntity tile = world.getBlockTileEntity(i, j, k);
		if (tile instanceof TileQuarry) {
			TileQuarry quarry = (TileQuarry) tile;
			Box box = quarry.box;
			if (box.isInitialized() && Integer.MAX_VALUE != box.xMax) {
				// X - Axis
				for (int x = box.xMin; x <= box.xMax; x++) {
					markFrameForDecay(world, x, box.yMin, box.zMin);
					markFrameForDecay(world, x, box.yMax, box.zMin);
					markFrameForDecay(world, x, box.yMin, box.zMax);
					markFrameForDecay(world, x, box.yMax, box.zMax);
				}

				// Z - Axis
				for (int z = box.zMin + 1; z <= box.zMax - 1; z++) {
					markFrameForDecay(world, box.xMin, box.yMin, z);
					markFrameForDecay(world, box.xMax, box.yMin, z);
					markFrameForDecay(world, box.xMin, box.yMax, z);
					markFrameForDecay(world, box.xMax, box.yMax, z);
				}

				// Y - Axis
				for (int y = box.yMin + 1; y <= box.yMax - 1; y++) {

					markFrameForDecay(world, box.xMin, y, box.zMin);
					markFrameForDecay(world, box.xMax, y, box.zMin);
					markFrameForDecay(world, box.xMin, y, box.zMax);
					markFrameForDecay(world, box.xMax, y, box.zMax);
				}
			}
			if (quarry != null) {
				dropContent(0, quarry, world, quarry.xCoord, quarry.yCoord,
						quarry.zCoord);
			}
			quarry.destroy();
		}

		Utils.preDestroyBlock(world, i, j, k);
		super.breakBlock(world, i, j, k, par5, par6);
	}

	public void dropContent(int newSize, IInventory chest, World world,
			int xCoord, int yCoord, int zCoord) {
		Random random = new Random();
		for (int l = newSize; l < chest.getSizeInventory(); l++) {
			ItemStack itemstack = chest.getStackInSlot(l);
			if (itemstack == null) {
				continue;
			}
			float f = random.nextFloat() * 0.8F + 0.1F;
			float f1 = random.nextFloat() * 0.8F + 0.1F;
			float f2 = random.nextFloat() * 0.8F + 0.1F;
			while (itemstack.stackSize > 0) {
				int i1 = random.nextInt(21) + 10;
				if (i1 > itemstack.stackSize) {
					i1 = itemstack.stackSize;
				}
				itemstack.stackSize -= i1;
				EntityItem entityitem = new EntityItem(world, (float) xCoord
						+ f, (float) yCoord + (newSize > 0 ? 1 : 0) + f1,
						(float) zCoord + f2, new ItemStack(itemstack.itemID,
								i1, itemstack.getItemDamage()));
				float f3 = 0.05F;
				entityitem.motionX = (float) random.nextGaussian() * f3;
				entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F;
				entityitem.motionZ = (float) random.nextGaussian() * f3;
				if (itemstack.hasTagCompound()) {
					entityitem.item.setTagCompound((NBTTagCompound) itemstack
							.getTagCompound().copy());
				}
				world.spawnEntityInWorld(entityitem);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer entityPlayer, int par6, float par7, float par8,
			float par9) {
		if (world.isRemote) {
			return true;
		} else if (entityPlayer.isSneaking()) {
			return false;
		} else {
			entityPlayer.openGui(QuarryPlus.instance,
					QuarryPlus.guiIdContainerQuarry, world, x, y, z);
			return true;
		}
	}

	@Override
	public String getTextureFile() {
		return "/org/yogpstop/qp/blocks.png";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}