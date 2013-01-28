package org.yogpstop.qp;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotQuarry extends Slot {

	public SlotQuarry(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack is) {
		if (is.itemID == QuarryPlus.itemBase.itemID) {
			switch (slotNumber) {
			case 0:
				return is.getItemDamage() == 1;
			case 1:
			case 2:
			case 3:
				return is.getItemDamage() == 2;
			default:
				return is.getItemDamage() == 3;
			}
		}
		return false;
	}
}
