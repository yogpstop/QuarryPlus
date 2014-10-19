package com.yogpc.mc_lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

public class WorkbenchRecipe {
  private static final List<WorkbenchRecipe> recipes = new ArrayList<WorkbenchRecipe>();
  public final ItemStack[] input;
  public final ItemStack output;
  public final double power;

  private WorkbenchRecipe(final ItemStack out, final double p, final ItemStack... in) {
    this.input = in;
    this.output = out;
    this.power = p;
  }

  public static void addRecipe(final ItemStack out, final double p, final ItemStack... in) {
    recipes.add(new WorkbenchRecipe(out, p, in));
  }

  public static List<WorkbenchRecipe> getRecipes() {
    return recipes;
  }

  boolean check(final ItemStack[] sinv) {
    for (final ItemStack raw : this.input) {
      final ItemStack miss = raw.copy();
      for (int i = 0; i < 27; i++) {
        final ItemStack is = sinv[i];
        if (is == null || is.stackSize <= 0)
          continue;
        if (!is.isItemEqual(miss))
          continue;
        if (!ItemStack.areItemStackTagsEqual(is, miss))
          continue;
        final int toMove = Math.min(is.stackSize, miss.stackSize);
        is.stackSize -= toMove;
        miss.stackSize -= toMove;
      }
      if (miss.stackSize > 0)
        return false;
    }
    return true;
  }

  static int getRecipes(final ItemStack[] inv, final WorkbenchRecipe[] rs, final int p) {
    final WorkbenchRecipe prev = p > 0 ? rs[p] : null;
    Arrays.fill(rs, null);
    Arrays.fill(inv, 27, 45, null);
    int cur = 27;
    for (final WorkbenchRecipe r : recipes) {
      final ItemStack[] sinv = new ItemStack[27];
      for (int i = 0; i < sinv.length; i++)
        if (inv[i] != null)
          sinv[i] = inv[i].copy();
      if (!r.check(sinv))
        continue;
      inv[cur] = r.output.copy();
      rs[cur] = r;
      cur++;
    }
    if (prev != null)
      for (int i = 27; i < 45; i++)
        if (rs[i] == prev)
          return i;
    return -1;
  }
}
