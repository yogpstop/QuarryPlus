package com.yogpc.qp.bc;

import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.api.recipes.IRefineryRecipeManager;
import buildcraft.api.recipes.IRefineryRecipeManager.IRefineryRecipe;

import com.yogpc.qp.TileRefinery;

public class RROld {
  private static void use(final FluidStack fs, final TileRefinery tr) {
    if (fs == null)
      return;
    for (final FluidStack s : tr.src) {
      if (!fs.isFluidEqual(s))
        continue;
      final int min = Math.min(s.amount, fs.amount);
      s.amount -= min;
      fs.amount -= min;
      if (fs.amount <= 0)
        break;
    }
    for (int i = tr.src.length - 1; i >= 0; i--)
      if (tr.src[i] != null && tr.src[i].amount == 0)
        tr.src[i] = null;
  }

  static void get(final TileRefinery tr) {
    if (tr.cached != null)
      return;
    final IRefineryRecipeManager irrm = BuildcraftRecipes.refinery;
    if (irrm == null)
      return;
    final IRefineryRecipe r = irrm.findRefineryRecipe(tr.src[0], tr.src[1]);
    if (r != null && RefineryRecipeHelper.check(r.getResult(), tr)) {
      use(r.getIngredient1(), tr);
      use(r.getIngredient2(), tr);
      tr.rem_energy = r.getEnergyCost();
      tr.rem_time = r.getTimeRequired();
      tr.cached = r.getResult().copy();
      tr.cached.amount *= tr.getEfficiency() + 1;
      RefineryRecipeHelper.get(tr);
    } else {
      tr.rem_energy = 0;
      tr.rem_time = 0;
      tr.cached = null;
    }
  }
}
