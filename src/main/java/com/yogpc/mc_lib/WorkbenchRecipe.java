package com.yogpc.mc_lib;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

public class WorkbenchRecipe {
  private static final List<WorkbenchRecipe> recipes = new ArrayList<WorkbenchRecipe>();
  private final ItemStack[] input;
  private final ItemStack output;
  private final double power;

  private WorkbenchRecipe(final ItemStack out, final double p, final ItemStack... in) {
    this.input = in;
    this.output = out;
    this.power = p;
  }

  public static void addRecipe(final ItemStack out, final double p, final ItemStack... in) {
    recipes.add(new WorkbenchRecipe(out, p, in));
  }

  ItemStack add(final EntityPlayer e, final TileWorkbench t) {
    // add item
    final ItemStack p = e.getCurrentEquippedItem();
    if (p != null) {
      boolean added = false;
      for (final ItemStack b : t.buffer) {
        if (!b.isItemEqual(p) || !ItemStack.areItemStackTagsEqual(b, p))
          continue;
        b.stackSize += p.stackSize;
        added = true;
        break;
      }
      if (!added)
        t.buffer.add(p);
      e.inventory.setInventorySlotContents(e.inventory.currentItem, null);
    }
    // check status
    if (t.getStoredEnergy() < this.power)
      return null;
    for (final ItemStack in : this.input) {
      boolean found = false;
      for (final ItemStack bf : t.buffer) {
        if (!in.isItemEqual(bf) || !ItemStack.areItemStackTagsEqual(in, bf)
            || in.stackSize > bf.stackSize)
          continue;
        found = true;
        break;
      }
      if (!found)
        return null;
    }
    // do crafting
    t.useEnergy(this.power, this.power, true);
    final List<ItemStack> toRemove = new ArrayList<ItemStack>();
    for (final ItemStack in : this.input)
      for (final ItemStack bf : t.buffer) {
        if (!in.isItemEqual(bf) || !ItemStack.areItemStackTagsEqual(in, bf)
            || in.stackSize > bf.stackSize)
          continue;
        bf.stackSize -= in.stackSize;
        if (bf.stackSize == 0)
          toRemove.add(bf);
        break;
      }
    for (final ItemStack rm : toRemove)
      t.buffer.remove(rm);
    return this.output.copy();
  }

  String getOutput() {
    return this.output.getDisplayName();
  }

  void sendDesc(final EntityPlayer e, final TileWorkbench t) {
    e.addChatMessage(new ChatComponentText("Output: " + getOutput()));
    if (t.getStoredEnergy() < this.power)
      e.addChatMessage(new ChatComponentText("Missing Power: "
          + Double.toString(this.power - t.getStoredEnergy())));
    e.addChatMessage(new ChatComponentText(":Missing Inputs:"));
    for (final ItemStack in : this.input) {
      int missing = in.stackSize;
      for (final ItemStack bf : t.buffer) {
        if (!in.isItemEqual(bf) || !ItemStack.areItemStackTagsEqual(in, bf))
          continue;
        missing -= bf.stackSize;
        break;
      }
      if (missing > 0)
        e.addChatMessage(new ChatComponentText(Integer.toString(missing) + " : "
            + in.getDisplayName()));
    }
  }

  WorkbenchRecipe next() {
    int i = recipes.indexOf(this) + 1;
    if (i >= recipes.size())
      i = 0;
    return recipes.get(i);
  }

  static WorkbenchRecipe first() {
    return recipes.get(0);
  }
}
