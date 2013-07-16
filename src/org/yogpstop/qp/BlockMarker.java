package org.yogpstop.qp;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import static buildcraft.BuildCraftCore.markerModel;
import static buildcraft.core.CreativeTabBuildCraft.tabBuildCraft;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeDirection;

public class BlockMarker extends Block implements ITileEntityProvider {

	public BlockMarker(int i) {
		super(i, Material.circuits);
		setLightValue(0.5F);
		setCreativeTab(tabBuildCraft);
		setUnlocalizedName("MarkerPlus");
		this.isBlockContainer = true;
	}

	@Override
	public int getRenderType() {
		return markerModel;
	}

	private static AxisAlignedBB getBoundingBox(int meta) {
		double w = 0.15;
		double h = 0.65;

		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		switch (dir) {
		case DOWN:
			return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 1F - h, 0.5F - w, 0.5F + w, 1F, 0.5F + w);
		case UP:
			return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 0F, 0.5F - w, 0.5F + w, h, 0.5F + w);
		case SOUTH:
			return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 0.5F - w, 0F, 0.5F + w, 0.5F + w, h);
		case NORTH:
			return AxisAlignedBB.getAABBPool().getAABB(0.5F - w, 0.5F - w, 1 - h, 0.5F + w, 0.5F + w, 1);
		case EAST:
			return AxisAlignedBB.getAABBPool().getAABB(0F, 0.5F - w, 0.5F - w, h, 0.5F + w, 0.5F + w);
		default:
			return AxisAlignedBB.getAABBPool().getAABB(1 - h, 0.5F - w, 0.5F - w, 1F, 0.5F + w, 0.5F + w);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		AxisAlignedBB bb = getBoundingBox(meta);
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		return world.isBlockSolidOnSide(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir.getOpposite());
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		return side;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		dropTorchIfCantStay(world, x, y, z);
	}

	private void dropTorchIfCantStay(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		if (!canPlaceBlockOnSide(world, x, y, z, meta)) {
			dropBlockAsItem(world, x, y, z, this.blockID, 0);
			world.setBlock(x, y, z, 0);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileMarker();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockId) {
		if (!world.isRemote) ((TileMarker) world.getBlockTileEntity(x, y, z)).S_updateSignal();
		dropTorchIfCantStay(world, x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ep, int par6, float par7, float par8, float par9) {
		if (!world.isRemote) {
			Item equipped = ep.getCurrentEquippedItem() != null ? ep.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof ItemTool && ep.getCurrentEquippedItem().getItemDamage() == 0) {
				TileMarker.Link l = ((TileMarker) world.getBlockTileEntity(x, y, z)).obj;
				if (l == null) return true;
				ep.sendChatToPlayer("This Marker's Area is:");
				ep.sendChatToPlayer(String.format("x:%d y:%d z:%d - x:%d y:%d z:%d", l.xn, l.yn, l.zn, l.xx, l.yn, l.zn));
				return true;
			}
			((TileMarker) world.getBlockTileEntity(x, y, z)).S_tryConnection();
		}
		return true;
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int meta) {
		((TileMarker) world.getBlockTileEntity(x, y, z)).requestTicket();
		super.onPostBlockPlaced(world, x, y, z, meta);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		world.removeBlockTileEntity(x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		this.blockIcon = par1IconRegister.registerIcon("yogpstop/quarryplus:marker");
	}

	@Override
	public boolean onBlockEventReceived(World par1World, int par2, int par3, int par4, int par5, int par6) {
		super.onBlockEventReceived(par1World, par2, par3, par4, par5, par6);
		TileEntity tileentity = par1World.getBlockTileEntity(par2, par3, par4);
		return tileentity != null ? tileentity.receiveClientEvent(par5, par6) : false;
	}
}
