package com.yogpc.mc_lib;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotWorkbench extends Slot {
  public SlotWorkbench(final IInventory p_i1824_1_, final int p_i1824_2_, final int p_i1824_3_,
      final int p_i1824_4_) {
    super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
  }

  @Override
  public boolean isItemValid(final ItemStack is) {
    return false;
  }
}
