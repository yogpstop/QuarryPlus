package org.yogpstop.qp;

import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.LiquidStack;

public class InfVolatLiquidTank implements ILiquidTank {
	final LiquidStack ls;

	InfVolatLiquidTank(LiquidStack vls) {
		this.ls = vls;
	}

	@Override
	public LiquidStack getLiquid() {
		return this.ls;
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
		int amount = Math.min(maxDrain, this.ls.amount);
		if (amount == 0) return null;
		if (doDrain) this.ls.amount -= amount;
		return new LiquidStack(this.ls.itemID, amount, this.ls.itemMeta, this.ls.extra);
	}

	@Override
	public int getTankPressure() {
		return 0;
	}

}
