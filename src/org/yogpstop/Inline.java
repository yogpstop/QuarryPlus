package org.yogpstop;

public final class Inline {
	public static final boolean isLiquid(net.minecraft.block.Block b) {
		return b == null ? false : (b instanceof net.minecraftforge.liquids.ILiquid || b instanceof net.minecraft.block.BlockFluid || b.blockMaterial
				.isLiquid());
	}
}
