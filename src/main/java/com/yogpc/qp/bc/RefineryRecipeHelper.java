package com.yogpc.qp.bc;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleCrafter;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.recipes.IRefineryRecipeManager;

import com.yogpc.qp.TileRefinery;

public class RefineryRecipeHelper implements IFlexibleCrafter {
  private final TileRefinery tile;

  private RefineryRecipeHelper(final TileRefinery tr) {
    this.tile = tr;
  }

  @Override
  public int getCraftingItemStackSize() {
    return 0;
  }

  @Override
  public ItemStack getCraftingItemStack(final int slotid) {
    return null;
  }

  @Override
  public ItemStack decrCraftingItemgStack(final int slotid, final int val) {
    return null;
  }

  @Override
  public FluidStack getCraftingFluidStack(final int id) {
    return this.tile.src[id];
  }

  @Override
  public FluidStack decrCraftingFluidStack(final int id, final int val) {
    final FluidStack ret = this.tile.src[id];
    if (ret == null)
      return null;
    if (val >= ret.amount) {
      this.tile.src[id] = null;
      return ret;
    }
    this.tile.src[id] = ret.copy();
    this.tile.src[id].amount -= val;
    ret.amount = val;
    return ret;
  }

  @Override
  public int getCraftingFluidStackSize() {
    return this.tile.src.length;
  }

  public static void get(final TileRefinery tr) {
    if (tr.cached != null)
      return;
    final IRefineryRecipeManager irrm = BuildcraftRecipeRegistry.refinery;
    if (irrm == null)
      return;
    for (final IFlexibleRecipe<FluidStack> ifr : irrm.getRecipes()) {
      final CraftingResult<FluidStack> cr = ifr.craft(new RefineryRecipeHelper(tr), true);
      if (cr == null || !RefineryRecipeHelper.check(cr.crafted, tr))
        continue;
      ifr.craft(new RefineryRecipeHelper(tr), false);
      tr.rem_energy = cr.energyCost;
      tr.rem_time = cr.craftingTime;
      tr.cached = cr.crafted.copy();
      tr.cached.amount *= tr.getEfficiency() + 1;
      RefineryRecipeHelper.get(tr);
      return;
    }
    tr.rem_energy = 0;
    tr.rem_time = 0;
    tr.cached = null;
  }

  private static boolean check(final FluidStack fs, final TileRefinery tr) {
    return tr.res == null || tr.res.isFluidEqual(fs)
        && tr.buf - tr.res.amount >= fs.amount * (tr.getEfficiency() + 1);
  }
}
