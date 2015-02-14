package com.yogpc.qp.bc;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.ModAPIManager;

public class BuildCraftHelper {
  public static final boolean isWrench(final Item equipped, final EntityPlayer ep, final int x,
      final int y, final int z) {
    if (ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools") && equipped instanceof IToolWrench
        && ((IToolWrench) equipped).canWrench(ep, x, y, z)) {
      ((IToolWrench) equipped).wrenchUsed(ep, x, y, z);
      return true;
    }
    return false;
  }
}
