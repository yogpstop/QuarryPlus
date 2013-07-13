package org.yogpstop.qp;

import java.util.Map;

import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;

public class InfVolatLiquidTank implements ILiquidTank {
	private final Map<Integer, Integer> list;
	private int id;

	InfVolatLiquidTank(int vid, Map<Integer, Integer> vlist) {
		this.id = vid;
		this.list = vlist;
	}

	@Override
	public LiquidStack getLiquid() {
		return new LiquidStack(this.id, this.list.containsKey(this.id) ? this.list.get(this.id) : 0);
	}

	@Override
	public int getCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int fill(LiquidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public LiquidStack drain(int maxDrain, boolean doDrain) {
		int amount = Math.min(maxDrain, this.list.containsKey(this.id) ? this.list.get(this.id) : 0);
		if (amount == 0) return null;
		if (doDrain) this.list.put(this.id, this.list.get(this.id) - amount);
		return new LiquidStack(this.id, amount);
	}

	@Override
	public int getTankPressure() {
		return 0;
	}

}
