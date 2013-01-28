package org.yogpstop.qp;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotQuarry extends Slot {

	public SlotQuarry(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		if (itemStack.itemID == QuarryPlus.itemBase.itemID) {
			return true;
		}
		return false;
	}
}
