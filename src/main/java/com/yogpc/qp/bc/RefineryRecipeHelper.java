package com.yogpc.qp.bc;

import net.minecraftforge.fluids.FluidStack;

import com.yogpc.qp.TileRefinery;

public class RefineryRecipeHelper {
  public static void get(final TileRefinery tr) {

  }

  static boolean check(final FluidStack fs, final TileRefinery tr) {
    return tr.res == null || tr.res.isFluidEqual(fs)
        && tr.buf - tr.res.amount >= fs.amount * (tr.getEfficiency() + 1);
  }
}
