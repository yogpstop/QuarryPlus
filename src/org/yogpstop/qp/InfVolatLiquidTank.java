package org.yogpstop.qp;

import java.util.HashMap;

import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;

public class InfVolatLiquidTank implements ILiquidTank {
	private static final HashMap<Integer, Integer> liquids = new HashMap<Integer, Integer>();
	static final int[] mapping = new int[ForgeDirection.VALID_DIRECTIONS.length];

	static InfVolatLiquidTank get(ForgeDirection fd) {
		if (fd.ordinal() < 0 || fd.ordinal() > ForgeDirection.VALID_DIRECTIONS.length || !liquids.containsKey(mapping[fd.ordinal()])) return null;
		return new InfVolatLiquidTank(liquids.get(mapping[fd.ordinal()]));
	}

	private int id;

	private InfVolatLiquidTank(int vid) {
		this.id = vid;
	}

	@Override
	public LiquidStack getLiquid() {
		return new LiquidStack(this.id, liquids.containsKey(this.id) ? liquids.get(this.id) : 0);
	}

	@Override
	public int getCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int fill(LiquidStack resource, boolean doFill) {
		return 0;
	}

	static void fill(int key, int amount) {
		if (!liquids.containsKey(key)) liquids.put(key, 0);
		liquids.put(key, liquids.get(key) + amount);
	}

	@Override
	public LiquidStack drain(int maxDrain, boolean doDrain) {
		int amount = Math.min(maxDrain, liquids.containsKey(this.id) ? liquids.get(this.id) : 0);
		if (amount == 0) return null;
		if (doDrain) liquids.put(this.id, liquids.get(this.id) - amount);
		return new LiquidStack(this.id, amount);
	}

	@Override
	public int getTankPressure() {
		return 0;
	}

}
