package org.yogpstop;

public final class Inline {
	public static final boolean isLiquid(net.minecraft.block.Block b) {
		return b == null ? false : (b instanceof net.minecraftforge.liquids.ILiquid || b instanceof net.minecraft.block.BlockFluid || b.blockMaterial
				.isLiquid());
	}

	public static final void removeFromWorld(org.yogpstop.qp.TileMarker.Link l, net.minecraft.tileentity.TileEntity tx) {
		if (isMine(l, tx)) {
			((org.yogpstop.qp.TileMarker) tx).obj = null;
			org.yogpstop.qp.QuarryPlus.blockMarker.dropBlockAsItem(tx.worldObj, tx.xCoord, tx.yCoord, tx.zCoord,
					org.yogpstop.qp.QuarryPlus.blockMarker.blockID, 0);
			tx.worldObj.setBlockToAir(tx.xCoord, tx.yCoord, tx.zCoord);
		}
	}

	public static final void removeFromWorld(org.yogpstop.qp.TileMarker.Link l, net.minecraft.tileentity.TileEntity tx,
			java.util.Collection<net.minecraft.item.ItemStack> c) {
		if (isMine(l, tx)) {
			((org.yogpstop.qp.TileMarker) tx).obj = null;
			c.addAll(org.yogpstop.qp.QuarryPlus.blockMarker.getBlockDropped(tx.worldObj, tx.xCoord, tx.yCoord, tx.zCoord, 0, 0));
			tx.worldObj.setBlockToAir(tx.xCoord, tx.yCoord, tx.zCoord);
		}
	}

	public static final boolean isMine(org.yogpstop.qp.TileMarker.Link l, net.minecraft.tileentity.TileEntity tx) {
		if (tx instanceof org.yogpstop.qp.TileMarker && ((org.yogpstop.qp.TileMarker) tx).obj == l) return true;
		return false;
	}

	public static final void removeConnection(org.yogpstop.qp.TileMarker.Link l, net.minecraft.tileentity.TileEntity tx) {
		if (isMine(l, tx)) ((org.yogpstop.qp.TileMarker) tx).obj = null;
	}
}
