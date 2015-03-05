package com.yogpc.qp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class WorkbenchRecipe {
  public static final class WBIS {
    public final double weight;
    public final Item item;
    public final NBTTagCompound tag;
    public final int meta;

    private WBIS(final ItemStack is) {
      this.weight = is.stackSize / 50.0;
      this.item = is.getItem();
      this.tag = is.stackTagCompound != null ? (NBTTagCompound) is.stackTagCompound.copy() : null;
      this.meta = is.getItemDamage();
    }

    static WBIS[] convert(final ItemStack[] s) {
      final WBIS[] r = new WBIS[s.length];
      for (int i = 0; i < s.length; i++)
        r[i] = new WBIS(s[i]);
      return r;
    }

    public int getAmount() {
      return (int) Math.floor(difficulty * this.weight);
    }

    public ItemStack getItemStack() {
      final ItemStack ret = new ItemStack(this.item, getAmount(), this.meta);
      ret.setTagCompound(this.tag);
      return ret;
    }
  }

  public static double difficulty;
  private static final List<WorkbenchRecipe> recipes = new ArrayList<WorkbenchRecipe>();
  public final WBIS[] input;
  public final ItemStack output;
  public final double power;

  private WorkbenchRecipe(final ItemStack out, final double p, final ItemStack... in) {
    this.input = WBIS.convert(in);
    this.output = out;
    this.power = p;
  }

  public static void addRecipe(final ItemStack out, final double p, final ItemStack... in) {
    recipes.add(new WorkbenchRecipe(out, p, in));
  }

  public static List<WorkbenchRecipe> getRecipes() {
    return recipes;
  }

  public boolean check(final ItemStack[] sinv) {
    for (final WBIS raw : this.input) {
      int miss = raw.getAmount();
      for (int i = 0; i < 27; i++) {
        if (miss <= 0)
          break;
        final ItemStack is = sinv[i];
        if (is == null || is.stackSize <= 0)
          continue;
        if (is.getItem() != raw.item || is.getItemDamage() != raw.meta)
          continue;
        if (is.stackTagCompound == null && raw.tag != null ? true : is.stackTagCompound != null
            && !is.stackTagCompound.equals(raw.tag))
          continue;
        final int toMove = Math.min(is.stackSize, miss);
        is.stackSize -= toMove;
        miss -= toMove;
      }
      if (miss > 0)
        return false;
    }
    return true;
  }

  public static int getRecipes(final ItemStack[] inv, final WorkbenchRecipe[] rs, final int p) {
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
