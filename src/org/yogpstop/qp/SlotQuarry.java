package org.yogpstop.qp;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class SlotQuarry extends Slot {

	public SlotQuarry(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		if (0 == this.getSlotIndex()) {
			if (itemStack.itemID == QuarryPlus.itemSilktouch.shiftedIndex) {
				return true;
			}
			return false;
		} else if (1 <= this.getSlotIndex() && this.getSlotIndex() <= 3) {
			if (itemStack.itemID == QuarryPlus.itemFortune.shiftedIndex) {
				return true;
			}
			return false;
		} else if (4 <= this.getSlotIndex() && this.getSlotIndex() <= 7) {
			if (itemStack.itemID == QuarryPlus.itemEfficiency.shiftedIndex) {
				return true;
			}
			return false;
		}
		return true;
	}
}
