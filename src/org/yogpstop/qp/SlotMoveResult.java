package org.yogpstop.qp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotMoveResult extends Slot {

	public SlotMoveResult(IInventory par1iInventory, int par2, int par3,
			int par4) {
		super(par1iInventory, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		return false;
	}
	
	@Override
	public boolean canTakeStack(EntityPlayer player){
		return false;
	}
}
